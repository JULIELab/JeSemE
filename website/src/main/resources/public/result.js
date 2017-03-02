 function decadeFormatter(year) {
            return year + "s"
    }
    
    function percentFormatter(value) {
        return value+"%"
    }

    function roundingFormatter(format, value) {
        return function(value){
        	return d3.format(format)(value)
        }
    }

function addWordTo(chart, url, wordBox, error) {
    let word2 = document.getElementById(wordBox).value
    document.getElementById(wordBox).value = ""
    document.getElementById(error).innerHTML = ""
    if (word2 !== "") {
        $.getJSON(url, {
        	corpus: corpus,
            word1: word,
            word2: word2
        }, function (data) {
            if (data.columns[0].length === 1)
                document.getElementById(error).innerHTML = "Interaction with \""+ word2 + "\" not covered by "+corpusName;
            else
                chart.load(data);
        });
    }
}
    
function addWordToFrequencyChart(chart, url, wordBox, error) {
    let word2 = document.getElementById(wordBox).value
    document.getElementById(wordBox).value = ""
    document.getElementById(error).innerHTML = ""
    if (word2 !== "") {
        $.getJSON(url, {
        	corpus: corpus,
            word: word2
        }, function (data) {
            if (data.columns[0].length === 1)
                document.getElementById(error).innerHTML ="\"" + word2 + "\" not covered by "+corpusName;
            else
                chart.load(data);
        });
    }
}

function addWordToTwo(chart, url, chart2, url2, wordBox, error) {
    let word2 = document.getElementById(wordBox).value
    document.getElementById(wordBox).value = ""
    document.getElementById(error).innerHTML = ""
    if (word2 !== "") {
        $.getJSON(url, {
        	corpus: corpus,
            word1: word,
            word2: word2
        }, function (data) {
            if (data.columns[0].length === 1)
                document.getElementById(error).innerHTML = "Interaction with \"" + word2 + "\" not covered by "+corpusName;
            else
                chart.load(data);
        });
        $.getJSON(url2, {
        	corpus: corpus,
            word1: word,
            word2: word2
        }, function (data) {
            if (data.columns[0].length !== 1)
                chart2.load(data);
        });
    }
}

function chart(bindto, url, y){
    var myy =  y != null ? y : {}
    if(!("tick" in myy)){
    	myy["tick"] = {
            format: roundingFormatter(".2")
    	}
    }
    var aChart = c3.generate({
        bindto: "#"+bindto,
        data: {"columns": [[]]},
        axis: 	{
                x: {
                    tick: {
                        format: decadeFormatter
                    }
                },
                y:myy
		},})
		var spinner = new Spinner().spin(document.getElementById(bindto))
		$(document).ready(	 $.getJSON(url, {
	     	 corpus: corpus,
	         word: word,
	     }, function (data) {
	    	 aChart.load(data);
	    	 spinner.stop()
	     }))
	return aChart 
 }
