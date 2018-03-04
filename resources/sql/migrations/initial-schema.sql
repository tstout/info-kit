create table LOG (
   id     int identity(1,1) primary key not null
  ,at     datetime not null
  ,level  varchar(32) not null
  ,msg    varchar(4096) not null
  ,logger varchar(200) not null
  ,thread varchar(200) not null
  ,ndc    varchar(1000)
);



