CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE storedconnection
(
	id BIGINT NOT NULL,
	type VARCHAR(255),
	name VARCHAR(255) unique,
	CONSTRAINT pk_connection PRIMARY KEY (id)
);

CREATE TABLE storedconnectionconfig
(
	id BIGINT NOT NULL,
	connection_id BIGINT,
	key VARCHAR(255),
	value VARCHAR(255),
	CONSTRAINT pk_storedconnectionconfig PRIMARY KEY (id),
	CONSTRAINT fk_storedconnectionconfig_on_connection FOREIGN KEY (connection_id) REFERENCES storedconnection (id)
);

insert into storedconnection (id, type, name)
	VALUES (nextval('hibernate_sequence'), 'mqtt_tasmota', 'mqtt_tasmota');

insert into storedconnectionconfig (id, connection_id, key, value) VALUES
(
	nextval('hibernate_sequence'),
	select id from storedconnection where name = 'mqtt_tasmota',
	'broker_host',
	'mqtt.seeseepuff.be'
);
