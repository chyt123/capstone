# capstone
Bittiger capstone project: electronic business recommendation system

# Usage
To run the project, make a directory "data/", and put three origin data files in it
Run TestApplication.java

# API
### POST /api/train?algo=itemknn
### POST /api/recommend
{
	"algo":"itemknn",
	"sourceData":[214838441, 214862111],
	"recommendNum":9
}

### POST /api/test
{
	"algo":"itemknn",
	"testFile":"yoochoose-test.dat",
	"recommendNum":8,
	"simThreshold":200,
    "sourceDataRatio":0.5,
    "sourceDataMin":150000,
    "sourceDataMax":200000
}
