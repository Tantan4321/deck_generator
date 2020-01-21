## Introduction

This is a command line deck generator application which will recursively scan
Wikipedia actresses and male actors pages for names, corresponding image URLs, and descriptions.
It attempts to pull information for 12,000 people into the deck.
The output JAR file is all that is needed to run this application.
It has been tested to run on Linux (Ubuntu 18.04) and Windows 10.

## Installation

1. You need to have Java 8 or higher installed on your PC.
2. Download the file: [deck_generator.jar](https://github.com/Tantan4321/deck_generator/tree/master/out/artifacts/deck_generator_jar)
to any directory on your PC.  
3. Open a command line window in the directory you put the file.
4. Then execute the following command:

```bash
java -jar deck_generator.jar
```

You will see a list of names scroll by as the application pulls data from
wikipedia to build the deck.

When the application completes there will be a **actors_actressess.zip** file containing the deck in the
same directory.

***NOTE***: A generated actors_actressess.zip file is included in the root directory of this repo as generating the deck takes ~10 mins.
