-- =====================================================
-- 1. SEQUÊNCIAS
-- =====================================================

CREATE SEQUENCE agendamento_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE consulta_id_seq
    START WITH 1
    INCREMENT BY 1;


-- =====================================================
-- 2. TABELA AGENDAMENTO
-- =====================================================

CREATE TABLE agendamento (
    id BIGINT NOT NULL DEFAULT nextval('agendamento_id_seq'),
    data_hora TIMESTAMP NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    status_agendamento VARCHAR(255) NOT NULL DEFAULT 'PENDENTE',
    status VARCHAR(255) NOT NULL DEFAULT 'ATIVO',
    animal_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,

    CONSTRAINT pk_agendamento PRIMARY KEY (id),

    CONSTRAINT fk_agendamento_animal
        FOREIGN KEY (animal_id)
        REFERENCES animal (id),

    CONSTRAINT fk_agendamento_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario (id)
);

CREATE INDEX idx_agendamento_animal ON agendamento (animal_id);
CREATE INDEX idx_agendamento_usuario ON agendamento (usuario_id);
CREATE INDEX idx_agendamento_data_hora ON agendamento (data_hora);


-- =====================================================
-- 3. TABELA CONSULTA
-- =====================================================

CREATE TABLE consulta (
    id BIGINT NOT NULL DEFAULT nextval('consulta_id_seq'),
    data_hora TIMESTAMP NOT NULL,
    diagnostico TEXT NOT NULL,
    tratamento TEXT NOT NULL,
    agendamento_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'ATIVO',

    CONSTRAINT pk_consulta PRIMARY KEY (id),

    CONSTRAINT fk_consulta_agendamento
        FOREIGN KEY (agendamento_id)
        REFERENCES agendamento (id),

    CONSTRAINT uk_consulta_agendamento UNIQUE (agendamento_id)
);