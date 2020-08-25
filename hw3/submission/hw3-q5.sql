-- Affected rows: 3.
-- 45s

select distinct F.dest_city as city
from Flights as F
where F.dest_city not in (select distinct T2.dest_city
                          from Flights as T1, Flights as T2
                          where T1.origin_city = 'Seattle WA'
                          and T1.dest_city = T2.origin_city)
and F.dest_city not in (select distinct T3.dest_city
                        from Flights as T3
                        where T3.origin_city = 'Seattle WA')
and F.dest_city <> 'Seattle WA';


--           CITY
--      Devils Lake ND
--      Hattiesburg/Laurel MS
--      St. Augustine FL
