-- public.service_record definition

-- Drop table

-- DROP TABLE public.service_record;

CREATE TABLE IF NOT EXISTS public.service_record
(
    id              bigserial   NOT NULL,
    customer_id     varchar(64) NOT NULL,
    service_type    varchar(32) NOT NULL,
    activation_date date NULL,
    expiration_date date NULL,
    amount          numeric(10, 2) NULL,
    status          varchar(20) NOT NULL,
    CONSTRAINT service_record_pkey PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_service_record_customer ON public.service_record USING btree (customer_id);
CREATE INDEX IF NOT EXISTS idx_service_record_expirpation_date ON public.service_record USING btree (expiration_date);
CREATE INDEX IF NOT EXISTS idx_service_record_status_customer ON public.service_record USING btree (status, customer_id);
CREATE INDEX IF NOT EXISTS idx_service_record_status_service_type ON public.service_record USING btree (status, service_type);

-- public.processing_error definition

-- Drop table

-- DROP TABLE public.processing_error;

CREATE TABLE IF NOT EXISTS public.processing_error
(
    id           bigserial   NOT NULL,
    "row_number" int4        NOT NULL,
    error_type   varchar(20) NOT NULL,
    message      text        NOT NULL,
    raw_row      text        NOT NULL,
    created_at   timestamp   NOT NULL,
    CONSTRAINT processing_error_pkey PRIMARY KEY (id)
);