var mainMenu = undefined


function registerRenderer(renderer, element) {
  element.rendos = renderer;
}

function initializeMainMenu(target, items) {
  mainMenu = target


  items.click(function (event) {
    var vnt = $(event);
    vnt.stop();

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
  lnk = $('#' + id)
  lnk.parent().parent().children(".active").removeClass("active")
  lnk.parent().addClass('active')

  $.ajax(lnk.attr('page'))
    .done(function (data) {
      mainMenu.html(data)
    })
    .fail(function (data) {
      alert(data.responseText);
    })
}


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

function setUpEditable(target, rendererMap) {
  target.click(function () {
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
      return;
    }

    if (!json.editable) {
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
      dv.remove();
      eventSource.show()
      wnd.unbind('resize', this)
    })
    crossButton.click(function () {
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
  event.stopPropagation()
  event.preventDefault()
  var dv = $("#newGatewayDiv")
  var frm = $("#newGatewayForm")
  frm[0].reset()
  dv.show()
}

function doCreateNew(theUrl) {
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
      triggerMainMenuItem("settings")
    }).
    fail(function (data) {
      showError(data.responseText)
      triggerMainMenuItem("settings")
    })
  dv.hide()
}

function doCancel() {
  event.stopPropagation()
  event.preventDefault()
  var dv = $("#newGatewayDiv")
  dv.hide()
}

function deleteGateway(id, theUrl) {
  event.stopPropagation()
  event.preventDefault()

  $.ajax({
    async: false,
    url: theUrl,
    success: function (data) {
      triggerMainMenuItem("settings")
    },
    fail: function (data) {
      triggerMainMenuItem("settings")
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