create database teste_banco;
use teste_banco;

create table usuarios (
id INT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR(100),
email VARCHAR(100),
senha VARCHAR(100)
);

select * from usuarios;