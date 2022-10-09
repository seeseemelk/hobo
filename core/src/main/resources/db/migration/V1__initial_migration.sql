CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE storeddevice
(
	id BIGINT NOT NULL,
	owner VARCHAR(255),
	name VARCHAR(255),
	CONSTRAINT pk_storeddevice PRIMARY KEY (id),
    CONSTRAINT uc_storeddevice_ownerName UNIQUE (owner, name)
);

CREATE TABLE storedboolproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT,
	name VARCHAR(255),
	reportTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requestTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requested BOOLEAN,
	reported BOOLEAN,
	CONSTRAINT pk_storedboolproperty PRIMARY KEY (id),
    CONSTRAINT fk_storedboolproperty_storeddevice FOREIGN KEY (device_id) REFERENCES storeddevice (id)
);

CREATE TABLE storedfloatproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT,
	name VARCHAR(255),
	reportTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requestTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requested FLOAT,
	reported FLOAT,
	CONSTRAINT pk_storedfloatproperty PRIMARY KEY (id),
    CONSTRAINT fk_storedfloatproperty_deviceId FOREIGN KEY (device_id) REFERENCES storeddevice (id)
);

CREATE TABLE storedintproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT,
	name VARCHAR(255),
	reportTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requestTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requested INTEGER,
	reported INTEGER,
	CONSTRAINT pk_storedintproperty PRIMARY KEY (id)
);

ALTER TABLE storedintproperty
	ADD CONSTRAINT fk_storedintproperty_on_device FOREIGN KEY (device_id) REFERENCES storeddevice (id);

CREATE TABLE storedstringproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT,
	name VARCHAR(255),
	reportTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requestTimestamp TIMESTAMP WITHOUT TIME ZONE,
	requested VARCHAR(255),
	reported VARCHAR(255),
	CONSTRAINT pk_storedstringproperty PRIMARY KEY (id)
);

ALTER TABLE storedstringproperty
	ADD CONSTRAINT fk_storedstringproperty_on_device FOREIGN KEY (device_id) REFERENCES storeddevice (id);
