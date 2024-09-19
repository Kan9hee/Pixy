/* globals Chart:false */

(() => {
  'use strict'

  const ctx = document.getElementById('myChart')
  const myChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: [
        'Prototype',
        '1.0'
      ],
      datasets: [{
        data: [
          0.53,
          0.67
        ],
        lineTension: 0,
        backgroundColor: 'transparent',
        borderColor: '#007bff',
        borderWidth: 4,
        pointBackgroundColor: '#007bff'
      }]
    },
    options: {
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          boxPadding: 3
        }
      },
      scales: {
        x: {
            display: false
        },
        y: {
            min: 0,
            max: 1
        }
      }
    }
  })
})()
