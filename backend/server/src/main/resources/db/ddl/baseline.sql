BEGIN;
-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	id bigserial NOT NULL,
	email varchar(255) NOT NULL,
	password_hash varchar(255) NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT users_email_key UNIQUE (email),
	CONSTRAINT users_pkey PRIMARY KEY (id)
);
-- public.restaurants definition

-- Drop table

-- DROP TABLE public.restaurants;

CREATE TABLE public.restaurants (
	id bigserial NOT NULL,
	"name" varchar(255) NOT NULL,
	cuisine varchar(255) NOT NULL,
	lat float8 NOT NULL,
	lng float8 NOT NULL,
	city varchar(255) NULL,
	pincode varchar(255) NULL,
	CONSTRAINT chk_lat CHECK (((lat >= ('-90'::integer)::double precision) AND (lat <= (90)::double precision))),
	CONSTRAINT chk_lng CHECK (((lng >= ('-180'::integer)::double precision) AND (lng <= (180)::double precision))),
	CONSTRAINT restaurants_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_restaurants_city ON public.restaurants USING btree (city);
CREATE INDEX idx_restaurants_cuisine_ci ON public.restaurants USING btree (lower((cuisine)::text));
CREATE INDEX idx_restaurants_name_ci ON public.restaurants USING btree (lower((name)::text));

-- public.dishes definition

-- Drop table

-- DROP TABLE public.dishes;

CREATE TABLE public.dishes (
	id bigserial NOT NULL,
	restaurant_id int8 NOT NULL,
	"name" varchar(255) NOT NULL,
	price int4 NOT NULL,
	CONSTRAINT chk_price_positive CHECK ((price > 0)),
	CONSTRAINT dishes_pkey PRIMARY KEY (id),
	CONSTRAINT uq_restaurant_dish UNIQUE (restaurant_id, name)
);


-- public.dishes foreign keys

ALTER TABLE public.dishes ADD CONSTRAINT dishes_restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES public.restaurants(id) ON DELETE CASCADE;
COMMIT;
