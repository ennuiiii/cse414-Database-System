DROP TABLE IF EXISTS Edges;

CREATE TABLE Edges(
  Source int,
  Destination int
);

INSERT INTO Edges
     VALUES (10,5), (6,25), (1,3), (4,4);

SELECT *
  FROM Edges;

SELECT Source
  FROM Edges;

SELECT *
  FROM Edges
 WHERE Source > Destination;

INSERT INTO Edges
     VALUES ('-1','2000');
-- As the documentation part3 mentions,
-- "Rigidly-typed database will convert the string '123' into an integer 123
-- prior to doing the insert.
-- Therefore, it doesn't violate the type INTEGER when we inserted the data
