create database PJI;
use PJI;

create table tipoConteudo (
	id int not null primary key auto_increment,
    nome varchar(250) not null
);

create table disciplina (
	id int not null primary key auto_increment,
    nome varchar(250) not null
);

create table psico (
	id int not null primary key auto_increment,
    nome varchar(250) not null,
    email varchar(250) not null,
    senha varchar(250) not null
);

create table professor (
	id int not null primary key auto_increment,
    nome varchar(250) not null,
    email varchar(250) not null,
    senha varchar(250) not null,
    telefone varchar(250) not null
);

create table professor_disciplina (
	idProfessor int not null, 
    idDisciplina int not null,
    foreign key (idProfessor) references professor(id),
    foreign key (idDisciplina) references disciplina(id)
);

create table serie (
	id int not null primary key,
    nome varchar(250) not null
);

insert into serie values (1, 'Primeiro ano');
insert into serie values (2, 'Segundo ano');
insert into serie values (3, 'Terceiro ano');
insert into serie values (4, 'Quarto ano');

create table aluno (
	id int not null primary key auto_increment,
    nome varchar(250) not null,
    dataNascimento DATE not null,
    hiperfoco varchar(250),
    tempoFoco decimal,
    idSerie int not null,
    nomeEscola varchar(250),
    laudo text not null,
    login varchar(250) not null,
    senha varchar(250) not null,
    idPsico int,
    foreign key (idPsico) references psico(id),
    foreign key (idSerie) references serie(id)
    
);

create table CID (
	idAluno int not null,
    numero varchar(250) not null,
    foreign key (idAluno) references aluno(id)
);

create table desregulacao (
	idAluno int not null,
    nomeclatura varchar(250) not null,
    foreign key (idAluno) references aluno(id)
);

create table aluno_professor (
	idProfessor int not null, 
    idAluno int not null,
    foreign key (idProfessor) references professor(id),
    foreign key (idAluno) references aluno(id)
);

create table atividade (
	id int not null primary key auto_increment,
    nome varchar(250) not null,
    idProfessor int,
    idPsico int,
    foreign key (idProfessor) references professor(id),
    foreign key (idPsico) references psico(id)
);

create table disciplina_atividade (
	idDisciplina int not null, 
    idAtividade int not null,
    foreign key (idDisciplina) references disciplina(id),
    foreign key (idAtividade) references atividade(id)
);

create table conteudoExtra (
	id int not null primary key auto_increment,
    titulo varchar(250) not null,
    idTipoConteudo int not null,
    idDiciplina int,
	foreign key (idTipoConteudo) references tipoConteudo(id),
    foreign key (idDiciplina) references disciplina(id)   
);
