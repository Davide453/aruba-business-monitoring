CREATE TABLE public.service_record (
	id bigserial NOT NULL,
	customer_id varchar(64) NOT NULL,
	service_type varchar(32) NOT NULL,
	activation_date date NULL,
	expiration_date date NULL,
	amount numeric(10, 2) NULL,
	status varchar(20) NOT NULL,
	CONSTRAINT service_record_pkey PRIMARY KEY (id)
);
CREATE TABLE public.processing_error (
	id bigserial NOT NULL,
	"row_number" int4 NOT NULL,
	error_type varchar(20) NOT NULL,
	message text NOT NULL,
	raw_row text NOT NULL,
	created_at timestamp NOT NULL,
	CONSTRAINT processing_error_pkey PRIMARY KEY (id)
);