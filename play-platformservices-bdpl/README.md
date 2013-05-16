    ...........................................................................
    ........:$$$7..............................................................
    .....==7$$$I~~...........MMMMMMMMM....DMM..........MM,........MM7......MM..
    ...,?+Z$$$?=~,,:.........MMM,,,?MMM+..MMM.........,MMMM,......7MM,....MMM..
    ..:+?$ZZZ$+==:,:~........MMM.....MMM..MMM.........,MMDMMM:.....,MMI..MMM...
    ..++7ZZZZ?+++====,.......MMM....~MMM..MMM.........,MM??DMMM:....?MM,MMM....
    ..?+OZZZ7~~~~OOI=:.......MMMMMMMMMM...MMM.........,MM?II?MMM~....DMMMM.....
    ..+7OOOZ?+==+7Z$Z:.......MMM$$$I,.....MMM.........,MM??8MMM~......NMM......
    ..:OOOOO==~~~+OZ+........MMM..........MMM.........,MMDMMM~........NMM......
    ..,8OOOO+===+$$?,........MMM..........MMM.,,,,,...,MMMM:..........NMM......
    ,,+8OOOZIIIIII=,,,,,,,,,,MMM,,,,,,,,,,NMMMMMMMMM=,,MM:,,,,........8MM......
    ,,,:O8OO~+~:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
                                                      ASCII Art: GlassGiant.com

Big Data Processing Language (BDPL)
===================================

Benchmark SRBench
-----------------
Below is a feature matrix of BDPL. It is based on
[SRBench: A Streaming RDF/SPARQL Benchmark](http://www.w3.org/wiki/SRBench) comparing
previous RDF streamin solutions SPARQLStream, CQELS and C-SPARQL. We used the
feature benchmark queries defined in SRBench and implemented them in BDPL. The results
are shown below.


|				| Q1	| Q2	| Q3	| Q4	| Q5	| Q6	| Q7	| Q8	| Q9	| Q10	| Q11	| Q12	| Q13	| Q14	| Q15	| Q16	| Q17	|
| ---:			| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	| :---:	|
|SPARQLStream	| ✓		| PP	| A		| G		| G		| ✓		| ✓		| G		| G,IF	| SD	| SD	| PP,SD	| PP,SD	| PP,SD	| PP,SD	| PP,SD	| PP,SD	|
|CQELS			| ✓		| PP	| A		| ✓		| ✓		| ✓		| D/N	| ✓		| IF	| ✓		| ✓		| PP	| PP	| PP	| PP	| PP	| PP	|
|C-SPARQL		| ✓		| PP	| A		| ✓		| ✓		| ✓		| D		| ✓		| IF	| ✓		| ✓		| PP	| PP	| PP	| PP	| PP	| PP	|
|BDPL			| ✓		| ✓(1,2)| A		| SM(3)	| ✓		| ✓(4)	| N(5)	| SM	| SM	| ✓		| ✓(6)	| PP,SM	| ✓(7,8)| ✓(9, 10)| ✓	| ✓		| ✓	(9)	|

Not supported by a particular system:
- **A:** Ask query form
- **D:** Dstream
- **G:** groupBy and aggregations
- **IF:** if expression
- **N:** negation
- **PP:** property path
- **SD:** static dataset

Special implementation remarks for BDPL:
- **SM:** no solutions modifiers implemented yet (e.g. `MIN(?values)` in CONSTRUCT clause)
- (1) property paths are replaced by RDFS reasoning
- (2) optional parts currently return a placeholder value
- (3) results reported not every 10 minutes but updated on each event (Sliding Window is used)
- (4) UNION can be replaces by BDPL `AND`-operator here
- (5) operator NOT is implemented in underlying ETALIS, must be included in BDPL
- (6) subselect can be replaced by BDPL `SEQ`-operator here
- (7) disjunctive property path replaced by BDPL `OR`-operator here
- (8) property paths in historical part are allowed in BDPL, we have higher expressivity here thanks to federation of queries
- (9) enhancement: fetching historic data first (using planned BDPL feature `CONTEXT`) 
- (10) enhancement: implement BDPL `REGEX` to be more powerful than the current BDPL `fn:contains`

Slash `/` means "or", either feature would be sufficient for the query to be sopported

Comma `,` means "and", all listed features must be supported for the query to work


References
----------
### SRBench
- Zhang, Y.; Duc, P.; Corcho, O. & Calbimonte, J.-P. (2012), SRBench: A Streaming RDF/SPARQL Benchmark, in Philippe Cudré-Mauroux; Jeff Heflin; Evren Sirin; Tania Tudorache; Jérôme Euzenat; Manfred Hauswirth; JosianeXavier Parreira; Jim Hendler; Guus Schreiber; Abraham Bernstein & Eva Blomqvist, ed., 'The Semantic Web – ISWC 2012', Springer Berlin Heidelberg, , pp. 641-657.
- http://www.w3.org/wiki/SRBench

### BDPL
- Stojanovic, N.; Stühmer, R.; Gibert, P. & Baude, F. (2012), Tutorial: Where Event Processing Grand Challenge meets Real-time Web: PLAY Event Marketplace, in 'Proceedings of the 6th ACM International Conference on Distributed Event-Based Systems'.
- https://github.com/play-project/play-dcep/tree/master/play-platformservices-bdpl
