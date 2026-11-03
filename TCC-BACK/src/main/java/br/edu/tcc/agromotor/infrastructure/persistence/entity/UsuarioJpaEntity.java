package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "perfil_usuario")
    private PerfilUsuario perfil;

    @Column(name = "registro_profissional")
    private String registroProfissional;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    protected UsuarioJpaEntity() {
    }

    public static UsuarioJpaEntity de(Usuario u) {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        e.id = u.id();
        e.nome = u.nome();
        e.email = u.email();
        e.senha = u.senhaHash();
        e.perfil = u.perfil();
        e.registroProfissional = u.registroProfissional();
        e.dataCriacao = u.dataCriacao();
        return e;
    }

    public Usuario paraDominio() {
        return new Usuario(id, nome, email, senha, perfil, registroProfissional, dataCriacao);
    }
}
