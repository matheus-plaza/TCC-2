import { Component, effect, ElementRef, inject, input, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, map, Observable, of, startWith, switchMap } from 'rxjs';
import { TermoGlossario } from '../../core/glossario';
import { mensagensDeErro } from '../../core/interceptors/error.interceptor';
import { Indicadores, IndicadoresRequest, LaudoRequest, MetodoPh, ProfundidadeCm } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { Ajuda } from '../../shared/ajuda';
import { hojeIso } from '../../shared/formato';
import { NumeroPipe } from '../../shared/pipes/numero.pipe';
import { TalhaoBusca } from '../../shared/talhao-busca';
import { aplicarErrosDeCampos, digitos, erroDeFormato } from '../../shared/validacao';

type CampoQuimico = Exclude<keyof LaudoRequest, 'talhaoId' | 'dataAnalise' | 'profundidadeCm' | 'metodoPh'>;

interface Campo {
  nome: CampoQuimico;
  rotulo: string;
  unidade: string;
  obrigatorio: boolean;
  min?: number;
  /** Teor que precisa ser maior que zero: o valor zero é recusado (RF03). */
  positivo?: boolean;
  max?: number;
  /** Dígitos inteiros e casas decimais aceitos, iguais aos do backend. */
  inteiros: number;
  decimais: number;
  ajuda?: TermoGlossario;
}

/** Campos da Tela A da especificação, na mesma ordem. */
const ATRIBUTOS: Campo[] = [
  { nome: 'ph', rotulo: 'pH', unidade: '', obrigatorio: true, min: 3, max: 9, inteiros: 2, decimais: 2 },
  { nome: 'moGDm3', rotulo: 'Matéria Orgânica', unidade: 'g/dm³', obrigatorio: true, positivo: true, inteiros: 3, decimais: 2 },
  { nome: 'pMehlich', rotulo: 'Fósforo P', unidade: 'mg/dm³', obrigatorio: true, positivo: true, inteiros: 3, decimais: 2, ajuda: 'mehlich' },
  { nome: 'kCmolc', rotulo: 'Potássio K⁺', unidade: 'cmolc/dm³', obrigatorio: true, positivo: true, inteiros: 2, decimais: 3 },
  { nome: 'caCmolc', rotulo: 'Cálcio Ca²⁺', unidade: 'cmolc/dm³', obrigatorio: true, positivo: true, inteiros: 2, decimais: 3 },
  { nome: 'mgCmolc', rotulo: 'Magnésio Mg²⁺', unidade: 'cmolc/dm³', obrigatorio: true, positivo: true, inteiros: 2, decimais: 3 },
  { nome: 'alCmolc', rotulo: 'Alumínio Al³⁺', unidade: 'cmolc/dm³', obrigatorio: true, min: 0, inteiros: 2, decimais: 3 },
  { nome: 'hAlCmolc', rotulo: 'Acidez Potencial H+Al', unidade: 'cmolc/dm³', obrigatorio: true, positivo: true, inteiros: 2, decimais: 3, ajuda: 'HAl' },
  { nome: 'argilaPorcentagem', rotulo: 'Argila', unidade: '%', obrigatorio: true, min: 0, max: 100, inteiros: 3, decimais: 2, ajuda: 'argila' },
];

const MICRONUTRIENTES: Campo[] = [
  { nome: 'znMgDm3', rotulo: 'Zinco Zn', unidade: 'mg/dm³', obrigatorio: false, min: 0, inteiros: 4, decimais: 2 },
  { nome: 'bMgDm3', rotulo: 'Boro B', unidade: 'mg/dm³', obrigatorio: false, min: 0, inteiros: 4, decimais: 2 },
  { nome: 'cuMgDm3', rotulo: 'Cobre Cu', unidade: 'mg/dm³', obrigatorio: false, min: 0, inteiros: 4, decimais: 2 },
  { nome: 'mnMgDm3', rotulo: 'Manganês Mn', unidade: 'mg/dm³', obrigatorio: false, min: 0, inteiros: 4, decimais: 2, ajuda: 'manganes' },
];

function controleDe(c: Campo): FormControl<number | null> {
  return new FormControl<number | null>(null, [
    ...(c.obrigatorio ? [Validators.required] : []),
    ...(c.min !== undefined ? [Validators.min(c.min)] : []),
    ...(c.max !== undefined ? [Validators.max(c.max)] : []),
    ...(c.positivo ? [Validators.min(0.001)] : []),
    digitos(c.inteiros, c.decimais),
  ]);
}

/** Tela A / RF03: registro do laudo analítico, com pré-visualização dos valores calculados. */
@Component({
  selector: 'app-laudo-form',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatRadioModule,
    MatButtonModule,
    MatIconModule,
    TalhaoBusca,
    Ajuda,
    NumeroPipe,
  ],
  templateUrl: './laudo-form.html',
  styleUrl: './laudo-form.scss',
})
export class LaudoForm {
  readonly talhaoIdUrl = input<string | undefined>(undefined, { alias: 'talhaoId' });

  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly atributos = ATRIBUTOS;
  protected readonly micronutrientes = MICRONUTRIENTES;
  protected readonly hoje = hojeIso();
  protected readonly talhaoId = signal<number | null>(null);
  protected readonly tentouSalvar = signal(false);
  protected readonly salvando = signal(false);
  protected readonly erros = signal<string[]>([]);

  protected readonly form = this.fb.group({
    dataAnalise: [this.hoje, Validators.required],
    profundidadeCm: ['0-20' as ProfundidadeCm, Validators.required],
    // Sem valor inicial: um método assumido daria a classe errada a um pH medido no outro
    metodoPh: [null as MetodoPh | null, Validators.required],
    quimica: this.fb.group(
      Object.fromEntries([...ATRIBUTOS, ...MICRONUTRIENTES].map((c) => [c.nome, controleDe(c)])) as Record<
        CampoQuimico,
        FormControl<number | null>
      >,
    ),
  });

  /** Pré-visualização: chama o backend, que guarda as fórmulas, quando os teores mudam. */
  protected readonly previa = toSignal<Indicadores | null>(this.previaDosIndicadores(), { initialValue: null });

  constructor() {
    effect(() => {
      const daUrl = Number(this.talhaoIdUrl());
      if (daUrl) {
        untracked(() => this.talhaoId.set(daUrl));
      }
    });
    // A lista de erros deixa de valer assim que o usuário começa a corrigir
    effect(() => {
      this.talhaoId();
      untracked(() => this.erros.set([]));
    });
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.erros.set([]));
  }

  private previaDosIndicadores(): Observable<Indicadores | null> {
    const quimica = this.form.controls.quimica;
    return quimica.valueChanges.pipe(
      startWith(quimica.value),
      debounceTime(250),
      map((): IndicadoresRequest | null => {
        const v = quimica.getRawValue();
        const c = quimica.controls;
        const validos = [c.caCmolc, c.mgCmolc, c.kCmolc, c.alCmolc, c.hAlCmolc].every((x) => x.valid);
        return validos
          ? { caCmolc: v.caCmolc!, mgCmolc: v.mgCmolc!, kCmolc: v.kCmolc!, alCmolc: v.alCmolc!, hAlCmolc: v.hAlCmolc! }
          : null;
      }),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      switchMap((req) => (req ? this.api.indicadores(req).pipe(catchError(() => of(null))) : of(null))),
    );
  }

  protected erro(c: Campo): string | null {
    const controle = this.form.controls.quimica.controls[c.nome];
    if (controle.hasError('required')) {
      return 'Obrigatório';
    }
    if (controle.hasError('min') && c.positivo) {
      return 'Deve ser maior que zero';
    }
    if (controle.hasError('min') || controle.hasError('max')) {
      return c.max !== undefined ? `Use um valor de ${c.min} a ${c.max}` : `Não pode ser menor que ${c.min}`;
    }
    return erroDeFormato(controle);
  }

  /** Lista cada campo incorreto, porque o botão de salvar pode estar longe deles na tela. */
  private camposIncorretos(): string[] {
    const incorretos = [...ATRIBUTOS, ...MICRONUTRIENTES].flatMap((c) => {
      const mensagem = this.erro(c);
      return mensagem ? [`${c.rotulo}: ${mensagem}`] : [];
    });
    if (this.form.controls.metodoPh.invalid) {
      incorretos.unshift('pH: informe se foi medido em CaCl₂ ou em H₂O');
    }
    if (this.form.controls.dataAnalise.invalid) {
      incorretos.unshift('Data da coleta: informe a data');
    }
    if (!this.talhaoId()) {
      incorretos.unshift('Talhão: selecione o talhão');
    }
    return incorretos;
  }

  private focarPrimeiroCampoIncorreto(): void {
    setTimeout(() => this.elemento.nativeElement.querySelector<HTMLInputElement>('input.ng-invalid, mat-radio-group.ng-invalid input')?.focus());
  }

  protected salvar(): void {
    this.tentouSalvar.set(true);
    const talhaoId = this.talhaoId();
    if (!talhaoId || this.form.invalid) {
      this.form.markAllAsTouched();
      this.erros.set(this.camposIncorretos());
      this.focarPrimeiroCampoIncorreto();
      return;
    }
    const raw = this.form.getRawValue();
    const q = raw.quimica;
    const req: LaudoRequest = {
      talhaoId,
      dataAnalise: raw.dataAnalise!,
      profundidadeCm: raw.profundidadeCm!,
      ph: q.ph!,
      metodoPh: raw.metodoPh!,
      moGDm3: q.moGDm3!,
      pMehlich: q.pMehlich!,
      kCmolc: q.kCmolc!,
      caCmolc: q.caCmolc!,
      mgCmolc: q.mgCmolc!,
      alCmolc: q.alCmolc!,
      hAlCmolc: q.hAlCmolc!,
      argilaPorcentagem: q.argilaPorcentagem!,
      znMgDm3: q.znMgDm3,
      bMgDm3: q.bMgDm3,
      cuMgDm3: q.cuMgDm3,
      mnMgDm3: q.mnMgDm3,
    };
    this.salvando.set(true);
    this.erros.set([]);
    this.api.cadastrarLaudo(req).subscribe({
      next: (laudo) => {
        this.snack.open('Laudo salvo', 'Fechar', { duration: 4000, panelClass: 'snack-ok' });
        this.router.navigate(['/laudos', laudo.id]);
      },
      error: (e) => {
        aplicarErrosDeCampos(e, this.form);
        this.erros.set(mensagensDeErro(e));
        this.salvando.set(false);
        this.focarPrimeiroCampoIncorreto();
      },
    });
  }
}
