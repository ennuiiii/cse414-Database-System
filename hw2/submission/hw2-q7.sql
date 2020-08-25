SELECT SUM(F.capacity) AS capacity
FROM   Flights AS F,
       Months AS M
WHERE  ((F.origin_city = 'Seattle WA' AND F.dest_city = 'San Francisco CA')
       OR(F.origin_city = 'San Francisco CA' AND F.dest_city = 'Seattle WA'))
AND    M.month = 'July'
AND    M.mid = F.month_id
AND    F.day_of_month = 10;
