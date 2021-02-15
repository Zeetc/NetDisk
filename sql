create database NetDisk;
create table user
(
    id           int auto_increment
        primary key,
    username     varchar(50) not null,
    password     varchar(50) not null,
    email        varchar(60) not null,
    registerDate datetime    not null,
    isManager    bit         not null
);

create table file
(
    fileId      int auto_increment
        primary key,
    filename    varchar(50)  not null,
    filePath    varchar(255) not null,
    fileSize    mediumtext   not null,
    isValidFile bit          not null,
    uploadDate  datetime     not null,
    contentType varchar(100) null,
    uid         int          not null,
    pid         int          not null,
    constraint file_user_id_fk
        foreign key (uid) references user (id)
);

