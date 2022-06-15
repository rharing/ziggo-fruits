/**
 * Created on 6/13/22.
 */

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import java.time.LocalDate

class FruitReader {
    String rootDir = ""
    FruitReader() {
        locateRootDir()
    }

    String read(String fileName, boolean writeFile = false) {
        String xmlContent = readFile(fileName)
        def result = new XmlParser().parseText(xmlContent)
        def colors = result.FruitColors[0].FruitColor.collectEntries {
            return [it.@id, it.@desc]
        }
        def types = result.FruitTypes[0].FruitType.collectEntries {
            return [it.@id, it.@desc]
        }
        def today = LocalDate.now()
        List<Fruit> fruits = []
        result.'**'.FruitRecord.FruitExpiryDate.findAll {
            Node it ->
                {
                    if (it) {
                        def expiryDateValue = it.value()[0].split("T")[0]
                        LocalDate expiryDate = LocalDate.parse(expiryDateValue)
                        if (expiryDate.isAfter(today)) {
                            Fruit fruit = new Fruit(type: it.parent().FruitType[0].value()[0], color: it.parent().FruitColor[0].value()[0])
                            fruits.add(fruit)
                            return null
                        } else {
                            return null
                        }
                    }
                    return null
                }
        }
        def totalFruits = fruits.inject([:]) { m, x -> if (!m[x]) m[x] = 0; m[x] += 1; m }
        def translatedFruits = translateFruits(totalFruits, types, colors)
        Map<Fruit, Integer> bananen = translatedFruits.findAll {
            it.key.type == "Banaan"
        }
        String outputFileName = "bananen_voor_${today}.json"
        return writeJson(outputFileName, bananen,writeFile)


    }

    static void main(String[] args) {
        FruitReader fruitReader = new FruitReader()
        fruitReader.read "resources/main/FruitExport_20220601.json"
//        FruitReader fruitReader = new FruitReader()
//        fruitReader.read("/home/ronald/projects/tryout/ziggo/fruits/src/main/resources/parsed.xml")
    }


    Map translateFruits(LinkedHashMap<Fruit, Integer> fruits, Map<String, String> types, Map<String, String> colors) {
        Map result = [:]
        def fruitColors = new JsonSlurper().parse(new File(rootDir+"/resources/main/FruitColorMap.json"))
        fruits.each {
            {
                result.put(translateFruit(it.key, types, colors, fruitColors ), it.value)
            }
        }
        return result
    }

    Fruit translateFruit(Fruit fruit, def types, def colors, def translatedFruitsAndColors) {
        def type = types.find { it.key == fruit.type }.value
        def color = colors.find { it.key == fruit.color }.value
        def fruitColors  = translatedFruitsAndColors.fruit[type].collectMany{it->it.findAll()}
        def productstate = fruitColors.find{
            it.color == color
        }.productState
        return new Fruit(type:type, productstate:productstate, color: color)
    }

    String readFile(String fileName) {
        File inputFile = new File(fileName)
        String xmlContent
         if(!inputFile.exists()){
             inputFile = new File( rootDir + fileName)
             if(!inputFile.exists()){
                 println">>>>>>>>>>> sorry cant read that file'$fileName' <<<<<<<<<<<<<<<<<<<<<<<<"
             }
             }
        if (fileName.endsWith("json")) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            def jsonContent = jsonSlurper.parseText(inputFile.text)
            xmlContent = new String(Base64.decoder.decode(jsonContent.fruitOrder.orderBase64), "UTF-8")
        }
        else{
            xmlContent = inputFile.text;
        }
        return xmlContent;

    }

    String writeJson(String fileName, Map<Fruit, Integer> bananen, boolean writeFile) {
        String json = createBananenOutputMap(bananen)
        if(writeFile) {
            new File(fileName).write(json)
        }
        return json
    }

    String createBananenOutputMap(Map<Fruit, Integer> bananen) {
        def result = [:]

        def bananenOutput = bananen.inject([:]) {
            map, key, value ->
                {
                    map.put(key.productstate, value)
                    return map
                }
        }
        def bananenMap = [:]
        bananenMap.put("Banaan", bananenOutput)
        result.put("fruit", bananenMap)
        def json = JsonOutput.toJson(result)
        return JsonOutput.prettyPrint(json)

    }

    void locateRootDir() {
        //hmm should have added spring just for easy locating of resources but this also works, took some more time then just setting
//        resource = new ClassPathResource
        // but now no need for fully bloated spring stuff
        rootDir = this.class.classLoader.getResource(".").toExternalForm()
        if(rootDir.startsWith("file:")) {
            rootDir = rootDir.substring(5)
        }
        rootDir = rootDir.split("/build/")[0]+"/build/"
        println rootDir
    }
}
