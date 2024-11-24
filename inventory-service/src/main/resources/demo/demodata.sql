-- Create demo vehicles
INSERT INTO vehicles (id, vin, make, model, year, price, status, sync_status) VALUES
                                                                                  ('550e8400-e29b-41d4-a716-446655440000', 'WBADT43403G093862', 'BMW', '3 Series', 2020, 35000, 'AVAILABLE', 'COMPLETED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440001', 'WAUUL78E45A013561', 'Audi', 'A4', 2021, 42000, 'PENDING', 'COMPLETED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440002', '1HGCM82633A123456', 'Honda', 'Accord', 2019, 28000, 'SOLD', 'COMPLETED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440003', '5YJSA1CN5DFP12345', 'Tesla', 'Model S', 2022, 89000, 'AVAILABLE', 'PROCESSING'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440004', 'JH4KA7532NC036794', 'Mercedes', 'C Class', 2021, 45000, 'AVAILABLE', 'COMPLETED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440005', '2T2BK1BA0FC123456', 'Lexus', 'RX', 2020, 52000, 'PENDING', 'FAILED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440006', '1G1BE5SM8H7123456', 'Chevrolet', 'Malibu', 2019, 25000, 'SOLD', 'COMPLETED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440007', '5NPE24AF1FH123456', 'Hyundai', 'Sonata', 2021, 27000, 'AVAILABLE', 'COMPLETED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440008', '1FM5K8F85FGA12345', 'Ford', 'Explorer', 2022, 48000, 'AVAILABLE', 'COMPLETED'),
                                                                                  ('550e8400-e29b-41d4-a716-446655440009', '4T1BF1FK1CU123456', 'Toyota', 'Camry', 2020, 29000, 'PENDING', 'COMPLETED');
