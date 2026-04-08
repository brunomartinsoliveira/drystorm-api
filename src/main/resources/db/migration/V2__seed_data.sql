-- V2__seed_data.sql
-- DryStorm - Dados Iniciais

-- Admin padrão (senha: Admin@123 - TROQUE EM PRODUÇÃO)
-- Hash gerado com BCrypt strength 12
INSERT INTO users (name, email, password, role)
VALUES ('Administrador', 'admin@drystorm.com.br',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY4/MWZmKQ7yN0G', 'ADMIN');

-- Serviços iniciais
INSERT INTO services (name, description, price, duration_minutes, category) VALUES
('Lavagem Simples',
 'Lavagem externa completa com produto neutro, enxágue e secagem com toalha de microfibra.',
 35.00, 30, 'LAVAGEM'),

('Lavagem Completa',
 'Lavagem externa + higienização interna (aspiração, limpeza do painel e tapetes).',
 65.00, 60, 'LAVAGEM'),

('Lavagem Premium',
 'Lavagem completa + cera líquida protetora e limpeza de borrachas com produto específico.',
 95.00, 90, 'LAVAGEM'),

('Polimento Simples',
 'Polimento de um estágio para remoção de riscos superficiais e oxidação leve.',
 250.00, 180, 'POLIMENTO'),

('Polimento Completo',
 'Polimento de dois estágios para correção de pintura com marcas e riscos médios.',
 450.00, 300, 'POLIMENTO'),

('Cristalização',
 'Aplicação de cristal de alta durabilidade para proteção e brilho intenso da pintura.',
 380.00, 240, 'PROTECAO'),

('PPF - Película Protetora',
 'Aplicação de película protetora de tinta (Paint Protection Film) em pontos críticos.',
 1200.00, 480, 'PROTECAO'),

('Higienização Interna',
 'Limpeza profunda do interior: aspiração, limpeza química de tecidos e plásticos.',
 180.00, 120, 'HIGIENIZACAO'),

('Higienização Completa',
 'Higienização interna completa com extratora, limpeza de couro e aromatização.',
 320.00, 180, 'HIGIENIZACAO'),

('Lavagem de Motor',
 'Limpeza completa do compartimento do motor com produto desengordurante e protetor.',
 120.00, 60, 'OUTROS');
