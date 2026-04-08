-- V1__initial_schema.sql
-- DryStorm - Schema Inicial

-- Tabela de Serviços
CREATE TABLE services (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    category    VARCHAR(50) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Usuários (admin)
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de Agendamentos
CREATE TABLE appointments (
    id                BIGSERIAL PRIMARY KEY,
    client_name       VARCHAR(100) NOT NULL,
    client_email      VARCHAR(150) NOT NULL,
    client_phone      VARCHAR(20) NOT NULL,
    client_vehicle    VARCHAR(100) NOT NULL,
    service_id        BIGINT NOT NULL REFERENCES services(id),
    appointment_date  DATE NOT NULL,
    appointment_time  TIME NOT NULL,
    end_time          TIME NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes             TEXT,
    confirmation_token VARCHAR(64),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointment_service FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Índices para performance
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_date_time ON appointments(appointment_date, appointment_time);
CREATE INDEX idx_services_active ON services(active);
CREATE UNIQUE INDEX idx_users_email ON users(email);

-- Comentários
COMMENT ON TABLE services IS 'Catálogo de serviços oferecidos pela DryStorm';
COMMENT ON TABLE appointments IS 'Agendamentos de serviços';
COMMENT ON TABLE users IS 'Usuários administradores do sistema';
