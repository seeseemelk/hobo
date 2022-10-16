CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE storeddevice
(
	id BIGINT NOT NULL,
	owner VARCHAR(255),
	name VARCHAR(255),
	CONSTRAINT pk_storeddevice PRIMARY KEY (id),
	CONSTRAINT uc_storeddevice_ownername UNIQUE (owner, name)
);

CREATE TABLE storedboolproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT REFERENCES storeddevice ON DELETE CASCADE,
	name VARCHAR(255),
	reportupdated TIMESTAMP WITHOUT TIME ZONE,
	requestupdated TIMESTAMP WITHOUT TIME ZONE,
	lastupdated TIMESTAMP WITHOUT TIME ZONE GENERATED ALWAYS AS ( GREATEST(reportupdated, requestupdated) ) stored,
	requested BOOLEAN,
	reported BOOLEAN,
	CONSTRAINT pk_storedboolproperty PRIMARY KEY (id)
);

CREATE TABLE storedfloatproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT REFERENCES storeddevice ON DELETE CASCADE,
	name VARCHAR(255),
	reportupdated TIMESTAMP WITHOUT TIME ZONE,
	requestupdated TIMESTAMP WITHOUT TIME ZONE,
	lastupdated TIMESTAMP WITHOUT TIME ZONE GENERATED ALWAYS AS ( GREATEST(reportupdated, requestupdated) ) stored,
	requested FLOAT,
	reported FLOAT,
	CONSTRAINT pk_storedfloatproperty PRIMARY KEY (id)
);

CREATE TABLE storedintproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT REFERENCES storeddevice ON DELETE CASCADE,
	name VARCHAR(255),
	reportupdated TIMESTAMP WITHOUT TIME ZONE,
	requestupdated TIMESTAMP WITHOUT TIME ZONE,
	lastupdated TIMESTAMP WITHOUT TIME ZONE GENERATED ALWAYS AS ( GREATEST(reportupdated, requestupdated) ) stored,
	requested INTEGER,
	reported INTEGER,
	CONSTRAINT pk_storedintproperty PRIMARY KEY (id)
);

CREATE TABLE storedstringproperty
(
	id BIGINT NOT NULL,
	device_id BIGINT REFERENCES storeddevice ON DELETE CASCADE,
	name VARCHAR(255),
	reportupdated TIMESTAMP WITHOUT TIME ZONE,
	requestupdated TIMESTAMP WITHOUT TIME ZONE,
	lastupdated TIMESTAMP WITHOUT TIME ZONE GENERATED ALWAYS AS ( GREATEST(reportupdated, requestupdated) ) stored,
	requested VARCHAR(255),
	reported VARCHAR(255),
	CONSTRAINT pk_storedstringproperty PRIMARY KEY (id)
);
