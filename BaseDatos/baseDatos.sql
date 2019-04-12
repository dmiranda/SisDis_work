CREATE TABLE jugadores (
	apodo CHAR(20),
	password CHAR(30),
	PRIMARY KEY (apodo));
	
CREATE TABLE tabla_partidas (
	apodo CHAR(20) REFERENCES jugadores (apodo),
	partidas INTEGER,
	ganadas	INTEGER,
	PRIMARY KEY (apodo));

\copy jugadores FROM jugadores.txt
\copy tabla_partidas FROM partidas.txt




