var mainMenu = undefined


function registerRenderer(renderer, element) {
  element.rendos = renderer;
}

function initializeMainMenu(target, items) {
  mainMenu = target


  items.click(function (event) {
    var vnt = $(event);
    vnt.stop();

    if (editorBusy)
      return;


    var trg = null
    if (window.event) {
      trg = window.event.srcElement
    } else {
      trg = event.target
    }

    lnk = $(trg)

    //alert(lnk.attr('href'))
    lnk.parent().parent().children(".active").removeClass("active")
    lnk.parent().addClass('active')

    $.ajax(lnk.attr('page'))
      .done(function (data) {
        mainMenu.html(data)
      })
      .fail(function (data) {
        alert(data.responseText);
      })
  })
}

function triggerMainMenuItem(id) {
  var vnt = $(window.event);
  vnt.stop();

  if (editorBusy)
    return;

  lnk = $('#' + id)
  lnk.parent().parent().children(".active").removeClass("active")
  lnk.parent().addClass('active')

  $.ajax(lnk.attr('page'))
    .done(function (data) {
      mainMenu.html(data)
      gotoHASH()
    })
    .fail(function (data) {
      alert(data.responseText);
    })
}

function gotoHASH() {
  if (location.hash) {
    //if ( $.browser.webkit == false ) {
    //  window.location.hash = location.hash;
    //} else {
      window.location.href = location.hash;
      window.location.href = location.hash;
    //}
  }
};


var htmlTemplate = '<div class="editable2" style="padding:4px">'
  + '<table class="zeromarg zeropad fullsize" >'
  + '<tr>'
  + '<td class="expand" style="vertical-align:top;padding:0;margin:0;line-height:0">'
  + '</td>'
  + '<td class="shrink">'
  + ''
  + '<table class="zeromarg zeropad fullsize" cellpadding="0" cellspacing="0">'
  + '<tr>'
  + '<td class="tiny check">'
  + '</td>'
  + '</tr>'
  + '<tr>'
  + '<td class="tiny cross">'
  + '</td>'
  + '</tr>'
  + '<tr>'
  + '</tr>'
  + '</table>'
  + '</td>'
  + '</tr>'
  + '</table>'
  + '</div>'

var editorBusy = false

function setUpEditable(target, rendererMap) {
  target.click(function (event) {

    if (editorBusy) {
      return;
    }

    editorBusy = true;
    eventSource = $(event.target)
    var binding = eventSource.attr("binding")
    if (binding === undefined) {
      eventSource = eventSource.parent()
      binding = eventSource.attr("binding")
      if (binding === undefined) {
        alert("Element is not bound")
        return;
      }
    }

    var renderer;

    var rendererName = eventSource.attr("renderer")
    if (rendererName === undefined) {
      renderer = function (data) {
        return data
      }
    } else {
      renderer = rendererMap[rendererName]
    }

    var json = null
    $.ajax(
      {
        url: binding,
        async: false
      }).done(function (data) {
      json = data
    }).fail(function (data) {
      alert("Fail\n" + data.responseText)
      json = null
    })

    if (!json) {
      editorBusy = false;
      return;
    }

    if (!json.editable) {
      editorBusy = false;
      return;
    }

    var dv = $(htmlTemplate)
    var checkButton = $('<button class="editorbutton">\u2713</button>')
    var crossButton = $('<button class="editorbutton">\u2718</button>')
    var theInput = createInput(json)

    dv.offset(function () {
      return eventSource.offset()
    })
    dv.width(function () {
      return eventSource.width() + 65
    })
    theInput.width(function () {
      return eventSource.width() + 20
    })
    theInput.height(function () {
      return eventSource.height()
    })

    var wnd = $(window)
    wnd.resize(function () {
      editorBusy = false;
      dv.remove();
      eventSource.show()
      wnd.unbind('resize', this)
    })
    crossButton.click(function () {
      editorBusy = false;
      dv.remove();
      eventSource.show()
    })

    checkButton.click(function () {
      var newUrl = binding + '&value=' + theInput.getData()
      $.ajax(
        {
          url: newUrl,
          async: false
        }).done(function (data) {
        json = data
      }).fail(function (data) {
        alert("Fail\n" + data.responseText)
        json = null
      })

      dv.remove();

      var data = theInput.getData()
      eventSource.html(renderer(data))
      eventSource.show()
      editorBusy = false;
    })

    eventSource.hide()

    dv.find('.check').append(checkButton)
    dv.find('.cross').append(crossButton)
    dv.find('.expand').append(theInput)
    dv.appendTo("body");
    theInput.focus()
  })
}

function createInput(json) {
  var inp = undefined
  if (json.type == 'string') {
    inp = $('<input class="fullsize transparentinput" type="text" value=""></input>')
    inp.getData = function () {
      return inp.val()
    }
    inp.val(json.value);
    return inp;
  } else if (json.type == 'int') {
    inp = $('<input type="number"value="10" name="some-name"/>')
    if (json.min !== undefined) {
      inp.attr("min", json.min)
    }
    if (json.max !== undefined) {
      inp.attr("max", json.max)
    }
    if (json.step !== undefined) {
      inp.attr("step", json.step)
    }
    inp.getData = function () {
      return inp.attr("value")
    }

    inp.val(json.value)
    return inp;
  } else if (json.type == 'list') {
    var inpStr = ' <select>'
    for (ndx in json.options) {
      var opt = json.options[ndx]
      inpStr += '<option value="' + opt.value + '">' + opt.label + '</option>'
    }
    inpStr += '</select>'

    inp = $(inpStr)

    inp.getData = function () {
      return inp.val()
    }

    inp.val(json.value)
  } else if (json.type == 'bool') {
    var inpStr = ' <select>'
    inpStr += '<option value="true">True</option>'
    inpStr += '<option value="false">False</option>'
    inpStr += '</select>'

    inp = $(inpStr)

    inp.getData = function () {
      return inp.val()
    }

    inp.val(json.value)
  } else {
    inp = $('<input class="fullsize transparentinput" type="text" value=""/>')
    inp.getData = function () {
      return inp.val()
    }

    inp.val(json.value)
  }

  return inp;
}

function createNew() {

  var vnt = $(window.event);
  vnt.stop();

  if (editorBusy)
    return;

  var vnt = $(window.event);
  vnt.stop();

  var dv = $("#newGatewayDiv")
  var frm = $("#newGatewayForm")
  frm[0].reset()
  dv.show()
}

function doCreateNew(theUrl) {

  var vnt = $(window.event);
  vnt.stop();

  if (editorBusy)
    return;
  event.stopPropagation()
  event.preventDefault()
  var dv = $("#newGatewayDiv")
  var frm = $("#newGatewayForm")

  $.ajax({
    type: 'POST',
    async: false,
    url: theUrl,
    data: frm.serialize()
  }).
  done(function (data) {
    alert(data)
    dv.hide()
  }).
  fail(function (data) {
    showError(data.responseText)
  })
}

function doCancel(event) {
  event.stopPropagation()
  event.preventDefault()
  var dv = $("#newGatewayDiv")
  dv.hide()
}

function undoDelete(theUrl){
  var vnt = $(window.event);
  vnt.stop();

  if (editorBusy)
    return;


  $.ajax({
    async: false,
    url: theUrl,
    success: function (data) {
      triggerMainMenuItem("gateways")
    },
    fail: function (data) {
      triggerMainMenuItem("gateways")
    }
  });
}

function deleteGateway(id, theUrl) {

  var vnt = $(window.event);
  vnt.stop();

  if (editorBusy)
    return;


  $.ajax({
    async: false,
    url: theUrl,
    success: function (data) {
      triggerMainMenuItem("gateways")
    },
    fail: function (data) {
      triggerMainMenuItem("gateways")
    }
  });
}

function showError(data) {
  $("#errorDialogContent").html(data);
  $("#errorDialog").show();
}

function disposeError() {
  $("#errorDialogContent").html('');
  $("#errorDialog").hide();
}