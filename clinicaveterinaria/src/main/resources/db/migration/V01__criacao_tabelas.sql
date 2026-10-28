-- =====================================================
-- 1. SEQUÊNCIAS
-- =====================================================

CREATE SEQUENCE usuario_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE prontuario_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE animal_id_seq
    START WITH 1
    INCREMENT BY 1;


-- =====================================================
-- 2. TABELA USUARIO
-- =====================================================

CREATE TABLE usuario (
    id BIGINT NOT NULL DEFAULT nextval('usuario_id_seq'),
    nome VARCHAR(255),
    email VARCHAR(255),
    senha VARCHAR(255),
    permissao VARCHAR(255),
    status VARCHAR(255) DEFAULT 'ATIVO',

    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);


-- =====================================================
-- 3. TABELA PRONTUARIO
-- =====================================================

CREATE TABLE prontuario (
    id BIGINT NOT NULL DEFAULT nextval('prontuario_id_seq'),
    alergias VARCHAR(255),
    doencas_pre_existentes VARCHAR(255),
    observacoes VARCHAR(255),
    status VARCHAR(255) DEFAULT 'ATIVO',

    CONSTRAINT pk_prontuario PRIMARY KEY (id)
);


-- =====================================================
-- 4. TABELA ANIMAL
-- =====================================================

CREATE TABLE animal (
    id BIGINT NOT NULL DEFAULT nextval('animal_id_seq'),
    nome VARCHAR(255),
    especie VARCHAR(255),
    data_nascimento DATE,
    sexo VARCHAR(255),
    peso DOUBLE PRECISION,
    usuario_id BIGINT,
    prontuario_id BIGINT,
    status VARCHAR(255) DEFAULT 'ATIVO',

    CONSTRAINT pk_animal PRIMARY KEY (id),

    CONSTRAINT fk_animal_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario (id),

    CONSTRAINT fk_animal_prontuario
        FOREIGN KEY (prontuario_id)
        REFERENCES prontuario (id),

    CONSTRAINT uk_animal_prontuario UNIQUE (prontuario_id)
);