# Home Office Settled Status Frontend

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally

    sm --start AGENTS_STUBS -f
    sbt run

It should then be listening on port 9386

    browse http://localhost:9386/home-office-settled-status

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
