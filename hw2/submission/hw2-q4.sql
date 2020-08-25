SELECT DISTINCT C.name AS name
FROM            Flights AS F,
                Carriers AS C
WHERE           F.carrier_id = C.cid
GROUP BY        C.cid,
                F.month_id,
                F.day_of_month
HAVING          COUNT(*) > 1000;
