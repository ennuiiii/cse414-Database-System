-- Affected rows: 4.
-- 3s

select distinct C.name as carrier
from Flights as F, Carriers as C
where F.carrier_id = C.cid
and F.origin_city = 'Seattle WA'
and F.dest_city = 'San Francisco CA'
order by carrier asc;

-- CARRIER
-- Alaska Airlines Inc.
-- SkyWest Airlines Inc.
-- United Air Lines Inc.
-- Virgin America
