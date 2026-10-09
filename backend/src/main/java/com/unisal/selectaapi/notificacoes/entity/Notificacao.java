    package com.unisal.selectaapi.notificacoes.entity;


    import com.unisal.selectaapi.notificacoes.NotificacoesRole;
    import com.unisal.selectaapi.user.entity.User;
    import jakarta.persistence.*;

    import java.time.LocalDateTime;
    import java.util.Set;
    import java.util.UUID;

    @Entity
    @Table(name = "notificacoes_usuarios")
    public class Notificacao {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID idNotificacao;

        @Column(name = "titulo", nullable = false)
        private String titulo;

        @Column(name = "descricao", nullable = false, length = 1000)
        private String descricao;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Enumerated(EnumType.STRING)
        @Column(name = "type", nullable = false)
        private NotificacoesRole type;

        @Column(nullable = false)
        private boolean lida = false;

        @Column(name = "lida_em")
        private LocalDateTime lidaEm;

        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        public Notificacao() {
        }

        public Notificacao(User user, NotificacoesRole type, String titulo, String descricao) {
            this.user = user;
            this.descricao = descricao;
            this.titulo = titulo;
            this.type = type;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        @PrePersist
        private void onCreate(){
            this.createdAt = LocalDateTime.now();
        }

        public LocalDateTime getLidaEm() {
            return lidaEm;
        }

        public void setLidaEm(LocalDateTime lidaEm) {
            this.lidaEm = lidaEm;
        }

        public boolean isLida() {
            return lida;
        }

        public void setLida(boolean lida) {
            this.lida = lida;
        }

        public UUID getIdNotificacao() {
            return idNotificacao;
        }

        public void setIdNotificacao(UUID idNotificacao) {
            this.idNotificacao = idNotificacao;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public NotificacoesRole getType() {
            return type;
        }

        public void setType(NotificacoesRole type) {
            this.type = type;
        }
    }
