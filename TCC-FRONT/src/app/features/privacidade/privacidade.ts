import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/** Aviso de privacidade: quais dados pessoais o sistema guarda, para quê, quem acessa e como excluir. */
@Component({
  selector: 'app-privacidade',
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <article class="privacidade">
      <a mat-button [routerLink]="auth.autenticado() ? '/' : '/auth/login'">
        <mat-icon>arrow_back</mat-icon> Voltar
      </a>

      <h1>Aviso de privacidade</h1>
      <p>Esta página explica quais dados pessoais o FertSolo guarda, para que eles servem, quem pode vê-los e como excluir a conta.</p>

      <h2>Dados guardados</h2>
      <ul>
        <li><strong>Nome e e-mail</strong>: identificam o usuário e servem para entrar no sistema.</li>
        <li><strong>Senha</strong>: guardada cifrada, apenas para conferir o login.</li>
        <li><strong>Registro profissional</strong> (agrônomo): identifica o responsável técnico no receituário.</li>
        <li>
          <strong>Propriedades</strong>, com seus talhões, laudos de solo, recomendações e registros de safra: servem para
          calcular as recomendações de calagem e adubação e acompanhar a evolução do solo.
        </li>
      </ul>

      <h2>Quem acessa</h2>
      <ul>
        <li><strong>Produtor</strong>: vê o próprio cadastro e os dados das próprias propriedades.</li>
        <li>
          <strong>Agrônomo</strong>: vê o cadastro e as propriedades dos produtores vinculados a ele. O vínculo é autorizado
          pelo produtor, criado quando o próprio agrônomo cadastra o produtor ou feito pelo administrador, e pode ser
          removido a qualquer momento.
        </li>
        <li>
          <strong>Administrador</strong>: gerencia contas e vínculos e vê apenas os dados de cadastro (nome, e-mail, perfil
          e registro profissional). Não tem acesso a propriedades, talhões, laudos nem recomendações. As ações dele sobre
          contas e vínculos ficam registradas.
        </li>
      </ul>

      <h2>Como excluir a conta</h2>
      <p>
        Depois de entrar, abra o menu com o seu nome, no canto superior, e escolha <strong>Excluir minha conta</strong>. A
        exclusão é irreversível.
      </p>
      <ul>
        <li>
          <strong>Produtor</strong>: são apagados o cadastro, as propriedades, os talhões, os laudos, as recomendações, os
          registros de safra e os vínculos.
        </li>
        <li>
          <strong>Agrônomo</strong>: são apagados o cadastro e os vínculos. As recomendações que ele registrou continuam com
          os produtores, sem o nome dele.
        </li>
        <li><strong>Administrador</strong>: é apagado o cadastro, desde que exista outro administrador.</li>
      </ul>
    </article>
  `,
  styles: `
    :host {
      display: block;
      padding: 32px 16px 48px;
    }
    .privacidade {
      max-width: 760px;
      margin: 0 auto;
      line-height: 1.6;
    }
    h1 {
      margin: 16px 0 8px;
    }
    h2 {
      margin: 28px 0 8px;
      font-size: 1.15rem;
    }
    ul {
      padding-left: 20px;
    }
    li {
      margin-bottom: 6px;
    }
  `,
})
export class Privacidade {
  protected readonly auth = inject(AuthService);
}
