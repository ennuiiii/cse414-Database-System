SELECT   C.name AS name,
         SUM(F.departure_delay) AS delay
FROM     Flights AS F,
         Carriers AS C
WHERE    C.cid = F.carrier_id
GROUP BY C.cid;
