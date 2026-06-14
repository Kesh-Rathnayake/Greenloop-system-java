USE greenloop;

CREATE TABLE IF NOT EXISTS email_notifications (
    notification_id INT(11) NOT NULL AUTO_INCREMENT,
    order_id INT(11) NOT NULL,
    client_id INT(11) NOT NULL,
    recipient_email VARCHAR(100) NOT NULL,
    subject VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    sent_status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (notification_id),
    KEY order_id (order_id),
    KEY client_id (client_id),

    CONSTRAINT email_notifications_ibfk_1
        FOREIGN KEY (order_id) REFERENCES orders(order_id),

    CONSTRAINT email_notifications_ibfk_2
        FOREIGN KEY (client_id) REFERENCES clients(client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


INSERT INTO clients (name, email, phone, address) VALUES
('Keells Super', 'thilini.silva815@gmail.com', '0771234567', 'Colombo'),
('Cargills Food City', 'thilini.silva815@gmail.com', '0772223333', 'Kandy'),
('Arpico Stores', 'thilini.silva815@gmail.com', '0775556666', 'Negombo'),
('Lanka Sathosa', 'thilini.silva815@gmail.com', '0774445555', 'Galle'),
('SPAR Outlet', 'thilini.silva815@gmail.com', '0778889999', 'Matara'),
('Softlogic Retail', 'thilini.silva815@gmail.com', '0779998888', 'Kurunegala'),
('Cool Planet', 'thilini.silva815@gmail.com', '0776667777', 'Nugegoda');

INSERT INTO orders
(client_id, amount, status, delivery_address, delivery_agent_id, scheduled_datetime, actual_delivery_date, delay_reason)
VALUES
((SELECT client_id FROM clients WHERE name = 'Keells Super' LIMIT 1),
 6100.00, 'Dispatched', 'Colombo 05', NULL, NOW(), CURDATE(), NULL),

((SELECT client_id FROM clients WHERE name = 'Cargills Food City' LIMIT 1),
 7800.00, 'Dispatched', 'Kandy', NULL, NOW(), CURDATE(), NULL),

((SELECT client_id FROM clients WHERE name = 'Arpico Stores' LIMIT 1),
 2950.00, 'Dispatched', 'Negombo', NULL, NOW(), CURDATE(), NULL),

((SELECT client_id FROM clients WHERE name = 'Lanka Sathosa' LIMIT 1),
 4200.00, 'Dispatched', 'Galle', NULL, NOW(), CURDATE(), NULL),

((SELECT client_id FROM clients WHERE name = 'SPAR Outlet' LIMIT 1),
 8500.00, 'Dispatched', 'Matara', NULL, NOW(), CURDATE(), NULL),

((SELECT client_id FROM clients WHERE name = 'Softlogic Retail' LIMIT 1),
 5600.00, 'Processing', 'Kurunegala', NULL, NULL, NULL, NULL),

((SELECT client_id FROM clients WHERE name = 'Cool Planet' LIMIT 1),
 3900.00, 'Pending', 'Nugegoda', NULL, NULL, NULL, NULL);