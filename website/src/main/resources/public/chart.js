 function decadeFormatter(year) {
            return year + "s"
    }
    
    function percentFormatter(value) {
        return value+"%"
    }

    function roundingFormatter(format, value) {
        return function(value){
        	console.log(value + "  " + d3.format(format)(value))
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

function chart(bindto, data, y){
    var myy =  y != null ? y : {}
    if(!("tick" in myy)){
    	myy["tick"] = {
            format: roundingFormatter(".2")
    	}
    }
    var aChart = c3.generate({
        bindto: bindto,
        data: data != null ? data : {"columns": [[]]},
        axis: 	{
                x: {
                    tick: {
                        format: decadeFormatter
                    }
                },
                y:myy
		},
		
// tooltip: {
// position: function () {
// var position = c3.chart.internal.fn.tooltipPosition.apply(this, arguments);
// position.top = 0;
// return position;
// },
// contents: function (data, defaultTitleFormat, defaultValueFormat, color) {
// var $$ = this, config = $$.config,
// titleFormat = config.tooltip_format_title || defaultTitleFormat,
// nameFormat = config.tooltip_format_name || function (name) { return name; },
// valueFormat = config.tooltip_format_value || defaultValueFormat,
// text, i, title, value;
// text = '<a href="http://google.de" target="_blank">test</a>'
	     
// return text;
// }
// }
		})
		
// aChart.internal.hideTooltip = function () {
// setTimeout(aChart, 10)
// };
	return aChart;    
 }