var arrowTickWidth = 20
var arrowTickHeight = 10
var arrowPadding = 10
var cW = 700
var cH = 700
var padding = 5
var logoWidth = 150
var backgroundColor = 'white'
var idleTrackFill = '#F7F7F7'
var idleTrackStroke = '#F0F0F0'
var readyTrackFill = '#68Ed6F'
var readyTrackFill2 = 'yellow'
var readyTrackStroke = 'blue'

var ibmLogo = null
var domibusLogo = null
var flameLogo = null
var holodeckLogo = null
var minderLogo = null
var greenBall = null
var ball = null
var redBall = null
var tenderIcon = null
var as4Icon = null
var context = null
var canvas = null

//Arrows
var Domibus2Minder = null
var Domibus2IBM = null
var Domibus2Holodeck = null
var Domibus2Flame = null

var IBM2Domibus = null
var IBM2Holodeck = null
var IBM2Flame = null
var IBM2Minder = null

var Holodeck2Domibus = null
var Holodeck2IBM = null
var Holodeck2Minder = null
var Holodeck2Flame = null

var Flame2Domibus = null
var Flame2IBM = null
var Flame2Holodeck = null
var Flame2Minder = null

var Minder2Domibus = null
var Minder2IBM = null
var Minder2Holodeck = null
var Minder2Flame = null

var width = 0
var height = 0
var centerX = 0
var centerY = 0

var animationTimeout = 10
var arrowArray = new Array()

function DirectedCurve(p1, p3, curvature) {
  var twoPi = 2 * Math.PI
  var translation = arrowTickWidth + arrowPadding
  var numTicks = length / (arrowPadding + arrowTickWidth)
  var documentLocation = 0
  var sending = false
  var currentColor = idleTrackFill
  var currentStroke = idleTrackStroke
  var that = this
  var document = null
  var counter = 0

  //we have p1,p2 and lets calculate p2 from the curvature

  //
  if (curvature === undefined) curvature = 15
  var radCurv = curvature * Math.PI / 180
  //vector
  var vec = [p3[0] - p1[0], p3[1] - p1[1]]
  //scale amount
  var len = Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1])
  var scale = 0.5 / Math.cos(radCurv)

  var newVec = [
    scale * (Math.cos(radCurv) * vec[0] - Math.sin(radCurv) * vec[1]),
    scale * (Math.sin(radCurv) * vec[0] + Math.cos(radCurv) * vec[1]),
  ]

  var p2 = [p1[0] + newVec[0], p1[1] + newVec[1]]


  var bez = new Bezier(p1, p2, p3)
  //calculate the bezier kernel for once and use it
  this.kernel = new Array()

  var stepSize = bez.getStepForSpacing(arrowPadding + arrowTickWidth)

  for (i = 0; i <= 1; i += stepSize / 7) {
    var arr = bez.calculateT(i)
    var angle = Math.atan2(arr[3], arr[2])
    this.kernel.push({x: arr[0], y: arr[1], theta: angle})
  }

  this.timeoutFunction = null

  //animates a sending of the given document
  this.send = function (document) {
    currentStroke = readyTrackStroke
    that.document = document
    documentLocation = 0
    timeoutFunction = setTimeout(that.animateSend, animationTimeout)
    return this
  }

  this.animateSend = function () {
    counter++
    documentLocation += 1
    if (documentLocation >= that.kernel.length) {
      that.stop()
      return
    }

    sending = true
    currentColor = Math.round(counter / 20) % 2 == 0 ? readyTrackFill : readyTrackFill2
    repaint()
    timeoutFunction = setTimeout(that.animateSend, animationTimeout)
  }

  this.stop = function () {
    var documentLocation = 0
    that.document = null
    clearTimeout(timeoutFunction)
    currentColor = idleTrackFill
    currentStroke = idleTrackStroke
    counter = 0
    repaint()
    that.finished()
  }

  this.finished = function () {
  }

  this.sent = function (func) {
    that.finished = func
  }

  //the coordinates for triangle
  var p11 = [-arrowTickWidth / 3, -arrowTickHeight / 2]
  var p12 = [-arrowTickWidth / 3, arrowTickHeight / 2]
  var p13 = [2 * arrowTickWidth / 3, 0]
  var p14 = [-arrowTickWidth / 3, -arrowTickHeight / 2]

  this.draw = function () {
    context.lineWidth = 1;
    for (i = 0; i < that.kernel.length; i += 8) {
      var currentStep = that.kernel[i];
      context.translate(currentStep.x, currentStep.y)
      context.rotate(currentStep.theta)

      context.fillStyle = currentColor
      context.strokeStyle = currentStroke
      context.beginPath()
      context.moveTo.apply(context, p11)
      context.lineTo.apply(context, p12)
      context.lineTo.apply(context, p13)
      context.lineTo.apply(context, p14)
      context.closePath()
      context.fill();
      context.stroke();

      context.rotate(-currentStep.theta)
      context.translate(-currentStep.x, -currentStep.y)
    }
    context.closePath()
    resetTransform()
    if (that.document != null) {
      var docLoc = that.kernel[documentLocation];
      //context.translate(docLoc.x, docLoc.y)
      context.drawImage(that.document.image, docLoc.x - that.document.width / 2, docLoc.y - that.document.height / 2)
      //context.translate(-docLoc.x, -docLoc.y)
    }

    //context.beginPath()
    //context.arc(p2[0] - 2, p2[1] - 2, 5, 0, 2 * Math.PI)
    //context.fillStyle = 'black'
    //context.fill()

  }
}

function resetTransform() {
  context.setTransform(1, 0, 0, 1, 0, 0)
}

function drawLogos() {
  ibmLogo.draw()
  holodeckLogo.draw()
  flameLogo.draw()
  domibusLogo.draw()
  minderLogo.draw()
}

var initialized = false
function checkImages() {
  if (ibmLogo === null ||
    domibusLogo === null ||
    flameLogo === null ||
    holodeckLogo === null ||
    minderLogo === null ||
    greenBall === null ||
    ball === null ||
    tenderIcon === null ||
    redBall === null)
    return

  if (initialized)
    return

  initialized = true

  initializeCanvas()
}

function initializeCanvas() {
  width = minderLogo.width / 10
  height = minderLogo.height / 10
  centerX = cW / 2
  centerY = cH / 2

  canvas = document.getElementById("animationCanvas");
  canvas.width = cW;
  canvas.height = cH;

  context = canvas.getContext("2d");
  //var radius = canvas.height / 2;
  //context.translate(radius, radius);
  radius = centerX * 0.90
  //setInterval(drawClock, 1000);


  flameLogo.setLocation(100, 100)
  ibmLogo.setLocation(600, 100)
  holodeckLogo.setLocation(100, 600)
  domibusLogo.setLocation(600, 600)
  minderLogo.setLocation(350, 350)

  Domibus2Minder = new DirectedCurve([600, 600], [350, 350])
  Domibus2IBM = new DirectedCurve([600, 600], [600, 100])
  Domibus2Holodeck = new DirectedCurve([600, 600], [100, 600])
  Domibus2Flame = new DirectedCurve([600, 600], [100, 100], 35)

  IBM2Domibus = new DirectedCurve([600, 100], [600, 600])
  IBM2Holodeck = new DirectedCurve([600, 100], [100, 600], 35)
  IBM2Flame = new DirectedCurve([600, 100], [100, 100])
  IBM2Minder = new DirectedCurve([600, 100], [350, 350])

  Holodeck2Domibus = new DirectedCurve([100, 600], [600, 600])
  Holodeck2IBM = new DirectedCurve([100, 600], [600, 100], 35)
  Holodeck2Minder = new DirectedCurve([100, 600], [350, 350])
  Holodeck2Flame = new DirectedCurve([100, 600], [100, 100])

  Flame2Domibus = new DirectedCurve([100, 100], [600, 600], 35)
  Flame2IBM = new DirectedCurve([100, 100], [600, 100])
  Flame2Holodeck = new DirectedCurve([100, 100], [100, 600])
  Flame2Minder = new DirectedCurve([100, 100], [350, 350])

  Minder2Domibus = new DirectedCurve([350, 350], [600, 600])
  Minder2IBM = new DirectedCurve([350, 350], [600, 100])
  Minder2Holodeck = new DirectedCurve([350, 350], [100, 600])
  Minder2Flame = new DirectedCurve([350, 350], [100, 100])

  arrowArray.push(Domibus2Minder)
  arrowArray.push(Domibus2Flame)
  arrowArray.push(Domibus2Holodeck)
  arrowArray.push(Domibus2IBM)
  arrowArray.push(IBM2Holodeck)
  arrowArray.push(IBM2Minder)
  arrowArray.push(IBM2Flame)
  arrowArray.push(IBM2Domibus)
  arrowArray.push(Minder2Domibus)
  arrowArray.push(Minder2IBM)
  arrowArray.push(Minder2Flame)
  arrowArray.push(Minder2Holodeck)
  arrowArray.push(Holodeck2IBM)
  arrowArray.push(Holodeck2Domibus)
  arrowArray.push(Holodeck2Minder)
  arrowArray.push(Holodeck2Flame)
  arrowArray.push(Flame2Domibus)
  arrowArray.push(Flame2IBM)
  arrowArray.push(Flame2Holodeck)
  arrowArray.push(Flame2Minder)

  repaint(context)
}

function repaint() {
  context.clearRect(0, 0, cW, cH)

  for (var i = 0; i < 20; ++i) {
    arrowArray[i].draw()
  }

  drawLogos()
}


function Bezier(p0, p1, p2) {
  var that = this

  /**
   * Calculate the coordinates and the first order derivative for the bezier curve at t,
   */
  that.calculateT = function (t) {
    var c1 = (1 - t) * (1 - t)
    var c4 = 2 * (1 - t)
    var c2 = c4 * t
    var c3 = t * t
    var c5 = 2 * t

    return [c1 * p0[0] + c2 * p1[0] + c3 * p2[0], c1 * p0[1] + c2 * p1[1] + c3 * p2[1],
      c4 * (p1[0] - p0[0]) + c5 * (p2[0] - p1[0]), c4 * (p1[1] - p0[1]) + c5 * (p2[1] - p1[1])]
  }

  that.length = function () {
    var a = {x: (p0[0] - 2 * p1[0] + p2[0]), y: (p0[1] - 2 * p1[1] + p2[1])}
    var b = {x: (2 * p1[0] - 2 * p0[0]), y: (2 * p1[1] - 2 * p0[1])}
    var A = 4 * (a.x * a.x + a.y * a.y);
    var B = 4 * (a.x * b.x + a.y * b.y);
    var C = b.x * b.x + b.y * b.y;

    var Sabc = 2 * Math.sqrt(A + B + C);
    var A_2 = Math.sqrt(A);
    var A_32 = 2 * A * A_2;
    var C_2 = 2 * Math.sqrt(C);
    var BA = B / A_2;

    return ( A_32 * Sabc +
        A_2 * B * (Sabc - C_2) +
        (4 * C * A - B * B) * Math.log((2 * A_2 + BA + Sabc) / (BA + C_2))
      ) / (4 * A_32);
  }

  this.getStepForSpacing = function (spacing) {
    var len = that.length()
    return spacing / len
  }
}

function Logo(image, resize) {
  if (resize === undefined) resize = false

  this.image = image
  if (resize) {
    this.width = logoWidth
    this.height = logoWidth * image.height / image.width
  } else {
    this.width = image.width
    this.height = image.height
  }
  var that = this

  this.setLocation = function (x, y) {
    this.location = {x: x, y: y}
    this.topLeft = {x: (x - this.width / 2), y: (y - this.height / 2)}
  }

  this.draw = function () {
    context.clearRect(that.topLeft.x - 5, that.topLeft.y - 5, that.width + 5, that.height + 5);
    context.fillStyle = backgroundColor
    context.fillRect(that.topLeft.x - 5, that.topLeft.y - 5, that.width + 5, that.height + 5);
    context.drawImage(this.image, that.topLeft.x, that.topLeft.y, that.width, that.height);
  }
}

function simulate() {
  var corner2 = $('#corner2').val()
  var corner3 = $('#corner3').val()

  if (corner2 === corner3 ){
    alert("Well, that's not funny!\nCorner2 and Corner3 cannot be the same.")
    return;
  }

  //determine arrows
  //minder to corner2
  var minder2Corner2 = window['Minder2' + corner2]
  var corner22corner3 = window[corner2 + '2' + corner3]
  var corner32Minder = window[corner3 + '2Minder']

  //put the arrows at the end of paint array, so that they stay over the other arrows
  arrowArray.splice(arrowArray.indexOf(minder2Corner2), 1)
  arrowArray.splice(arrowArray.indexOf(corner22corner3), 1)
  arrowArray.splice(arrowArray.indexOf(corner32Minder), 1)

  arrowArray.push(minder2Corner2)
  arrowArray.push(corner22corner3)
  arrowArray.push(corner32Minder)

  minder2Corner2.send(tenderIcon).sent(
    function () {
      corner22corner3.send(as4Icon).sent(
        function () {
          corner32Minder.send(tenderIcon)
        }
      )
    }
  )
}


function updateAnimationSpeed(newSpeed) {
  if (newSpeed == 0)
    newSpeed = 500

  animationTimeout = 500 / newSpeed

  $('#currentRate').html(animationTimeout)
}


