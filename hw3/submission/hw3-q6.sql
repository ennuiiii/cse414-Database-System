-- Affected rows: 4.
-- 4s

select T.name as carrier
from (select distinct C.name as name
      from Flights as F, Carriers as C
      where F.carrier_id = C.cid
      and F.origin_city = 'Seattle WA'
      and F.dest_city = 'San Francisco CA') as T
order by carrier asc;


--            CARRIER
--    Alaska Airlines Inc.
--    SkyWest Airlines Inc.
--    United Air Lines Inc.
--    Virgin America
