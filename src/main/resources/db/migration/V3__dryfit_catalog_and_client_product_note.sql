-- DryStorm — domínio loja de roupas dryfit (substitui catálogo automotivo legado)
-- Apaga agendamentos e serviços antigos incompatíveis com as novas categorias.

ALTER TABLE appointments RENAME COLUMN client_vehicle TO client_product_note;

DELETE FROM appointments;
DELETE FROM services;

INSERT INTO services (name, description, price, duration_minutes, category) VALUES
('Camiseta Dryfit Training',
 'Camiseta manga curta em tecido dryfit, leve e de secagem rápida — ideal para treinos intensos.',
 79.90, 30, 'CAMISETAS'),

('Camiseta Dryfit Sport',
 'Modelo esportivo com caimento atlético e costuras reforçadas para corridas e academia.',
 89.90, 30, 'CAMISETAS'),

('Camiseta Dryfit Long Sleeve',
 'Manga longa em dryfit com proteção UV leve para treinos ao ar livre.',
 99.90, 30, 'CAMISETAS'),

('Regata Dryfit Runner',
 'Regata cavada com respirabilidade máxima para corridas e dias quentes.',
 69.90, 20, 'REGATAS'),

('Regata Dryfit Muscle',
 'Corte mais largo nas costas, tecido dryfit de alta elasticidade.',
 74.90, 20, 'REGATAS'),

('Shorts Dryfit Performance',
 'Shorts com bolso interno para celular e cós elástico com cordão.',
 94.90, 30, 'SHORTS'),

('Bermuda Dryfit Treino',
 'Comprimento médio, tecido leve e secagem rápida para musculação e funcional.',
 109.90, 30, 'SHORTS'),

('Meia Dryfit Cano Médio',
 'Par de meias com compressão leve e zona acolchoada no calcanhar.',
 34.90, 15, 'ACESSORIOS'),

('Bandana / Munhequeira Dryfit',
 'Acessório multifuncional em dryfit para suor e estilo.',
 29.90, 15, 'ACESSORIOS'),

('Kit Camiseta + Shorts Dryfit',
 'Combo promocional: escolha cor e numeração na loja; atendimento personalizado.',
 159.90, 45, 'KITS'),

('Consulta e prova na loja',
 'Atendimento para tirar dúvidas sobre tamanhos, tecido dryfit e combinações — sem custo do produto.',
 0.00, 30, 'OUTROS');

COMMENT ON TABLE services IS 'Catálogo de produtos e experiências da loja DryStorm (roupas dryfit)';
COMMENT ON TABLE appointments IS 'Agendamentos de visita, prova ou retirada na loja DryStorm';
COMMENT ON COLUMN appointments.client_product_note IS 'Produto, tamanho ou cor informado pelo cliente';
