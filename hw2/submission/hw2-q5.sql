SELECT   C.name AS name,
         SUM(F.canceled = 1)*1.0/COUNT(*) AS percent
FROM     Carriers AS C,
         Flights AS F
WHERE    C.cid = F.carrier_id
AND      F.origin_city = 'Seattle WA'
GROUP BY C.cid
HAVING   percent > 0.005
ORDER BY percent ASC;
