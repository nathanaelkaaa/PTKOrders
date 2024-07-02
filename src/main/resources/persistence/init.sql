CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    id_customer INTEGER,
    id_product INTEGER,
    quantity INTEGER
);

INSERT INTO orders (id_customer, id_product, quantity) VALUES
    (1, 2, 1),
    (2, 1, 2),
    (2, 3, 1),
    (3, 3, 1);