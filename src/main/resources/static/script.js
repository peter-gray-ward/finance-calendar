var body = () => document.getElementsByTagName('body')[0];
var main = () => document.getElementById('main');
var modal = document.createElement('div');
    modal.classList.add('modal');
var calendar = () => document.getElementById('calendar');
let Api, Page, Months;
let process = {
  expanding: false,
  refreshing: false
}
var fc = {
  sync: () => {
    return new Promise(resolve => {
      var xhr = new XMLHttpRequest();
      xhr.open('GET', '/sync-data');
      xhr.addEventListener('load', function() {
        var res = JSON.parse(this.response);
        resolve(res);
      });
      xhr.send();
    });
  },
  api: (method, which, data = {}) => {
    return new Promise(resolve => {
      var xhr = new XMLHttpRequest();
      xhr.open(method, `/${which}`, true);
      xhr.setRequestHeader('content-type', 'application/json')
      xhr.addEventListener('load', function() {
        var res = JSON.parse(this.response);
        if (res.status == 'loggedout') {
          return location.reload()
        }
        resolve(res);
      });
      var req = data;
      try {
        req = JSON.stringify(data);
      } catch {
        throw new Error("invalid request body");
      }
      if (method == 'GET' || method == 'DELETE') {
        xhr.send()
      } else {
        xhr.send(req);
      }
    });
  },
  render: (which, data = {}) => {
    if (typeof data !== 'object') data = {};
    data.which = which;
    return new Promise(resolve => {
      var xhr = new XMLHttpRequest();
      xhr.open('PUT', '/render');
      xhr.addEventListener('load', function() {
        resolve(this.response);
      });
      xhr.send(JSON.stringify(data));
    });
  }
};

var SerializeAndSave = undefined;


function SerializeAndSaveExpense(id) {
  var row = document.getElementById(id);
  var json = JSON.stringify({
    name: row.children[0].value,
    frequency: row.children[1].children[0].value,
    amount: row.children[2].value,
    startdate: moment(row.children[3].value).format('yyyy-MM-DD'),
    recurrenceenddate: moment(row.children[4].value).format('yyyy-MM-DD'),
    expense_id: row.id,
  });
  fc.api('POST', Api.UPDATE_EXPENSE + '/' + id, json);
}

function SerializeAndSaveDebt(id) {
  var row = document.getElementById(id);
  var json = JSON.stringify({
    id: row.id,
    creditor: row.children[0].value,
    account_number: row.children[1].value,
    balance: row.children[2].value,
    interest: row.children[3].value,
    recurrenceid: row.children[5].dataset.recurrenceid
  });
  fc.api('POST', Api.UPDATE_DEBT + '/' + id, json);
}

function SerializeEvent() {
  var eventEdit = document.getElementById("event-edit");
  var modalEvent = eventEdit.children[0]
  return {  
    id: eventEdit.dataset.id,
    recurrenceid: eventEdit.dataset.recurrenceid,
    summary: document.querySelector('.upper-div-collection div[name="summary"]').innerHTML,
    date: moment($(modalEvent.querySelector('input[name="date"]')).val()).format('yyyy-MM-DD'),
    recurrenceenddate:  moment($(modalEvent.querySelector('input[name="recurrenceenddate"]')).val()).format('yyyy-MM-DD'), 
    amount: document.querySelector('.upper-div-collection input[name="amount"]').value,
    frequency: document.getElementById('time-container').querySelector('.select-container input[name="frequency"]').innerHTML
  };
}

function EditEvent(event, eventId) {
  fc.api('GET', Api.GET_EVENT + '/' + eventId).then(res => {
    if (res.status !== 'success') return alert(res.status)

    var modal = document.createElement('div');
    modal.classList.add('modal');
    modal.classList.add('event-modal')
    modal.innerHTML = res.template;

    body().appendChild(modal);


    var width = +getComputedStyle(modal).width.split('px')[0]
    var height = +getComputedStyle(modal).height.split('px')[0]
    var top = event.clientY
    var left = event.clientX

    while (top < 0) {
      top += 1
    }

     while (height + top > window.innerHeight) {
      top -= 1
    }

    while (width + left > window.innerWidth) {
      left -= 1
    }

     while (left < 0) {
      left += 1
    }

    modal.style.top = top + 'px';
    modal.style.left = left + 'px';


    

    ADD_EVENTS()
  });
}

var events = {
  '.select:focus': (event) => {
    var input = event.srcElement;
    input.classList.add('focusable')

    if (!input.parentElement.classList.contains('active')) {
      input.parentElement.classList.add('active');
      var options = eval(input.dataset.options);
      var width = getComputedStyle(input.parentElement.children[0]).width
      var i = 0;
      for (var option of options) {
        var optionEl = document.createElement('button');
        optionEl.classList.add('option');
        optionEl.innerHTML = `<span>${option}</span>`;
        optionEl.dataset.value = option

        // var graphic = document.createElement('div')
        // graphic.classList.add('option')
        // $(graphic).addClass('graphic')
        // $(graphic).html(input.dataset[option + '-graphic-text'])
        // optionEl.appendChild(graphic)

        optionEl.style.marginTop = (i++) * 25 + 'px'
        optionEl.style.width = width

        input.parentElement.appendChild(optionEl);
      }
      ADD_EVENTS()
    }
  },
  
  '.select:blur': function(event) {
    // var isOption = event.target
    // if (isOption.classList.contains('option')) return
    // while (isOption && isOption.classList.contains('option') == false) {
    //   isOption = isOption.parentElement
    // }
    // if (isOption) return
    // var input = event.srcElement;
    // var sc = $(event.target).closest('.select-container')
    // $(sc).removeClass('active');
    // sc[0].querySelectorAll('.option').forEach(el => el.remove());
  },

  '#save-this-event:click': (e) => {
    var event = SerializeEvent()
    fc.api('PUT', Api.SAVE_THIS_EVENT + '/' + event.id, event).then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').innerHTML = res.template
        $('.modal').addClass('saved')
        setTimeout(() => $('.modal').removeClass('saved'), 1000)
      }
    })
  },

  '#save-this-and-future-events:click': (e) => {
    var event = SerializeEvent()
    fc.api('PUT', Api.SAVE_THIS_AND_FUTURE_EVENTS + '/' + event.recurrenceid, event).then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').innerHTML = res.template
        $('.modal').addClass('saved')
        setTimeout(() => $('.modal').removeClass('saved'), 1000)
      }
    })
  },

  '#clude-this-event:click': e => {
    var eventId = e.srcElement
    var newButtonText = eventId.innerHTML == 'include' ? 'exclude' : 'include'
    while (eventId && !eventId.classList.contains('id')) {
      eventId = eventId.parentElement
    }
    eventId = eventId.dataset.id
    fc.api('GET', Api.CLUDE_THIS_EVENT + '/' + eventId).then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').innerHTML = res.template
        e.srcElement.innerHTML = newButtonText
        document.querySelector('#clude-all-these-events').innerHTML = newButtonText + ' all'
      }
    })
  },

  '#clude-all-these-events:click': e => {
    var eventRecurrenceid = e.srcElement
    var newButtonText = eventRecurrenceid.innerHTML == 'include all' ? 'exclude all' : 'include all'
    while (eventRecurrenceid && !eventRecurrenceid.classList.contains('id')) {
      eventRecurrenceid = eventRecurrenceid.parentElement
    }
    eventRecurrenceid = eventRecurrenceid.dataset.recurrenceid
    fc.api('GET', Api.CLUDE_ALL_THESE_EVENTS + '/' + eventRecurrenceid).then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').innerHTML = res.template
        e.srcElement.innerHTML = newButtonText
        document.querySelector('#clude-this-event').innerHTML = newButtonText.replace(' all', '')
      }
    })
  },

  'window:mousedown': (event) => {
    var isSelect = event.target
    while (isSelect && isSelect.classList.contains('select-container') == false) {
      isSelect = isSelect.parentElement;
    }
    if (!isSelect && document.querySelector('.select-container .option')) {
      $('.select-container').removeClass('active')
      document.querySelectorAll('.option').forEach(option => option.remove())
    }
    var isModal = event.target
    while (isModal && isModal.classList.contains('modal') == false) {
      isModal = isModal.parentElement;
    }
    var focusable = event.target
    while (focusable && focusable.classList.contains('focusable') == false) {
      focusable = focusable.parentElement;
    }
    var eventTitle = event.srcElement;
    while (eventTitle && eventTitle.classList.contains('event') == false) {
      eventTitle = eventTitle.parentElement;
    }
    if (isModal && !focusable) {
      isModal.classList.add('gripped')
      $(isModal).data().offset = JSON.stringify({
        x: event.clientX - $(isModal).offset().left,
        y: event.clientY - $(isModal).offset().top
      });
    } else if (!isModal) {
      if (document.querySelector('.modal')) {
        document.querySelector('.modal').remove()
      }
    }
    if (eventTitle) {
      EditEvent(event, eventTitle.id)
    }
  },

  'window:mousemove': (event) => {
    var modal = $(event.target).closest('.modal')
    if (modal.length) {
      if (modal.hasClass('gripped')) {
        var offset = JSON.parse(modal.data().offset);
        var newX = event.clientX - offset.x;
        var newY = event.clientY - offset.y;

        modal.css({
            left: `${newX}px`,
            top: `${newY}px`
        });

      }
    }
  },

  'window:mouseup': (event) => {
    var focusable = event.target
    while (focusable && !focusable.classList.contains('focusable')) focusable = focusable.parentElement
    var modal = event.target
    while (modal && modal.classList.contains('modal') == false) {
      modal = modal.parentElement
    }
    if (modal) {
      modal.classList.remove("gripped")
    }
  },

  '.expenses .td:keyup': function(event) {
    var id = $(event.srcElement).closest('.tr')[0].id;
    clearTimeout(SerializeAndSave)
    console.log('starting a new debounce at line:',104)
    SerializeAndSave = setTimeout(SerializeAndSaveExpense.bind(null, id), 200);
  },
  '.expenses .td:change': function(event) {
    var id = $(event.srcElement).closest('.tr')[0].id;
    clearTimeout(SerializeAndSave)
    console.log('starting a new debounce at line:',110)
    SerializeAndSave = setTimeout(SerializeAndSaveExpense.bind(null, id), 200);
  },

  '.debts .td:keyup': function(event) {
    var id = $(event.srcElement).closest('.tr')[0].id;
    clearTimeout(SerializeAndSave)
    SerializeAndSave = setTimeout(SerializeAndSaveDebt.bind(null, id), 900);
  },
  '.debts .td:change': function(event) {
    var id = $(event.srcElement).closest('.tr')[0].id;
    clearTimeout(SerializeAndSave)
    SerializeAndSave = setTimeout(SerializeAndSaveDebt.bind(null, id), 200);
  },

  'button.option:click': function(event) {
    var tr = event.target;
    while (tr && tr.classList.contains("tr") == false) {
      tr = tr.parentElement;
    }

    
    var id = $(event.srcElement).closest('.id')[0].id
    var name = $(event.srcElement).closest('.id')[0].dataset.name
    var selectContainer = $(event.srcElement).closest('.select-container')
    var input = $(selectContainer).find('input.select');
    var button = $(event.srcElement).closest('button.option')


    $(input).val(button.data().value);



    if (tr) {
      clearTimeout(SerializeAndSave)
      console.log('starting a new debounce at line:',173)
      switch (name) {
      case 'expense':
        SerializeAndSave = setTimeout(SerializeAndSaveExpense.bind(null, id), 2000);
        break
      default:
        break;
      }
    }
  },

  '.add-expense:click': function(event) {
    var expenses = $($(event.srcElement).closest('#left')).find('#expenses')[0]
    fc.api('POST', Api.ADD_EXPENSE).then(res => {
      if (res.status == 'success') {
        expenses.insertAdjacentHTML('beforeend', res.template);
        ADD_EVENTS()
      } else {
        console.error(res.error)
      }
    });
  },
  '.delete-expense:click': function(event) {
    var row = $(event.srcElement).closest('.tr.data')
    var expense_id  = row[0].id;
    fc.api('DELETE', Api.DELETE_EXPENSE + '/' + expense_id).then(res => {
      if (res.status == 'success') {
        row[0].remove();
      }
    });
  },
  '.delete-debt:click': function(event) {
    var row = $(event.srcElement).closest('.tr.data')
    var debt_id  = row[0].id;
    fc.api('DELETE', Api.DELETE_DEBT + '/' + debt_id).then(res => {
      if (res.status == 'success') {
        row[0].remove();
      }
    });
  },
  '#expand-to-budget:click': () => {
    if (process.expanding) return;
    process.expanding = true;
    if ($('body').hasClass('simple')) {
      $('body').removeClass('simple');
      teardownnews()
    }
    if ($('body').hasClass('complex') == false) {
      $('header').addClass('visible');
      setTimeout(function() {
        $('body').addClass('complex');
        process.expanding = false;
      }, 0);
    } else {
      $('body').removeClass('complex');
      setTimeout(function() {
        $('header').removeClass('visible');
        process.expanding = false;
      }, 100);
    }
  },

  '#expand-to-news:click': () => {
    if (process.expanding) return;
    process.expanding = true;


    if ($('body').hasClass('complex')) {
      $('body').removeClass('complex');
      $('header').removeClass('visible');
      process.expanding = false;
    }
    if ($('body').hasClass('simple') == false) {
      $('header').addClass('visible');
      setTimeout(function() {


        $('body').addClass('simple');
        initnews()

        process.expanding = false;
      }, 0);
    } else {

      $('body').removeClass('simple');
      teardownnews()

      setTimeout(function() {
        $('header').removeClass('visible');
        process.expanding = false;
      }, 100);
    }
  },

  '#refresh-calendar:click': function refreshData() {
    if (process.refreshing) return;
    process.refreshing = true;
    $('#refresh-calendar').addClass('refreshing');
    fc.api('GET', Api.REFRESH_CALENDAR).then(res => {
      $('#refresh-calendar').removeClass('refreshing');
      debugger
      process.refreshing = false;
      if (res.status == 'success') {
        document.getElementById('calendar').outerHTML = res.template;
        ADD_EVENTS()
        ScrollToFirstOfMonth();
      }
    });   
  },

  '#add-debt:click': function(event) {
    var debts = $($(event.srcElement).closest('#left')).find('#debts')[0]
    fc.api('POST', Api.ADD_DEBT).then(res => {
      if (res.status == 'success') {
        debts.insertAdjacentHTML('beforeend', res.template);
        ADD_EVENTS()
      } else {
        console.error(res.error)
      }
    });
  },

  '#prev-month:click': () => {
    fc.api('POST', Api.CHANGE_MONTH + '/prev').then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').outerHTML = res.template;
        document.getElementById('month-name').innerHTML = Months[res.data.month]
        document.getElementById('year-name').innerHTML = res.data.year
        ADD_EVENTS()
        ScrollToFirstOfMonth()
      }
    })
  },
  '#go-to-today:click': () => {
    fc.api('POST', Api.CHANGE_MONTH + '/this').then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').outerHTML = res.template;
        document.getElementById('month-name').innerHTML = Months[res.data.month]
        document.getElementById('year-name').innerHTML = res.data.year
        ADD_EVENTS()
        ScrollToFirstOfMonth()
      }
    })
  },
  '#next-month:click': () => {
    fc.api('POST', Api.CHANGE_MONTH + '/next').then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').outerHTML = res.template;
        document.getElementById('month-name').innerHTML = Months[res.data.month]
        document.getElementById('year-name').innerHTML = res.data.year
        ADD_EVENTS()
        ScrollToFirstOfMonth()
      }
    })
  },
  '#logout:click': () => {
    fc.api('POST', Api.LOGOUT).then(res => {
      location.reload();
    });
  },

  '#checking-balance:change': ChangeCheckingBalance,
  '#checking-balance:onpaste': ChangeCheckingBalance,
  '#checking-balance:keyup': ChangeCheckingBalance,

  '.create-payment-plan:click': e => {
    var row = $(e.target).closest('.tr')
    var id = row[0].id
    var creditor = $(row[0].children[0]).val()
    fc.api('GET', Api.CREATE_PAYMENT_PLAN + '/' + id).then(res => {
      if (res.status == 'success') {
        var planModal = document.createElement('div')
        planModal.id = 'debt-payment-plan'
        planModal.classList.add('modal')
        planModal.innerHTML = res.template
        document.body.appendChild(planModal)
      }
    })
  },

  '#delete-this-event:click': e => {
    var eventId = e.srcElement
    while (eventId && !eventId.classList.contains('id')) {
      eventId = eventId.parentElement
    }
    fc.api('DELETE', Api.DELETE_THIS_EVENT + '/' + eventId.dataset.id).then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').innerHTML = res.template
        document.querySelector('.modal').remove()
      }
    })
  },
  '#delete-all-these-events:click': e => {
    var eventId = e.srcElement
    while (eventId && !eventId.classList.contains('id')) {
      eventId = eventId.parentElement
    }
    fc.api('DELETE', Api.DELETE_ALL_THESE_EVENTS + '/' + eventId.dataset.recurrenceid).then(res => {
      if (res.status == 'success') {
        document.getElementById('calendar').innerHTML = res.template
      }
    })
  },

  '.day-block:dblclick': e => {
    var isEvent = e.srcElement
    while (isEvent && !isEvent.classList.contains('event')) {
      isEvent = isEvent.parentElement
    }
    if (!isEvent) {
      var date = e.srcElement
      while (date && !date.classList.contains('day-block')) {
        date = date.parentElement
      }

      fc.api('POST', Api.ADD_EVENT + '/' + date.dataset.year + '-' + date.dataset.month + '-' + date.dataset.date).then(res => {
        if (res.status == 'success') {
          document.getElementById('calendar').innerHTML = res.template
          EditEvent(e, res.eventId)
        }
      })
    }
  },

  '.news-outlet:click': e => {
    var outlet = e.srcElement.innerHTML
    var news_outlet_container = document.getElementById('news-outlet-' + outlet)
    if (news_outlet_container.classList.contains('active')) {
      news_outlet_container.classList.remove('active')
    } else {
      news_outlet_container.classList.add('active')
    }
  }

}

function ChangeCheckingBalance(e) {
  clearTimeout(SerializeAndSave)
  SerializeAndSave = setTimeout(() => {
    var value = $('#checking-balance').val()
    try {
      value = Number(value).toFixed(2)
      fc.api('POST', Api.SAVE_CHECKING_BALANCE + '/' + value).then(res => {
        if (res.status == 'success') {
          document.getElementById('calendar').innerHTML = res.template
          ScrollToFirstOfMonth()
          ADD_EVENTS()
        }
      })
    } catch (error) {
      console.error(error)
    }
    
  }, 800)
}

events['button.option:click'] = events['button.option:click'].bind(events)

fc.sync().then(res => {
  console.log(res)
  Api = res.data.api;
  Page = res.data.page;
  Months = res.data.months;

  ScrollToFirstOfMonth(0)
});


function ScrollToFirstOfMonth(offset = 0) {
  var fom = document.querySelectorAll('.first-of-month');
  fom = fom.length == 3 ? fom[1] : fom[0];
  var fomWeek = fom;
  while (fomWeek && fomWeek.classList.contains("week") == false) fomWeek = fomWeek.parentElement;
  var headerHeight = +getComputedStyle(document.getElementById('calendar-month-header')).height.split('px')[0];
  var weekHeaderHeight = +getComputedStyle(document.getElementById('calendar-week-header')).height.split('px')[0];
  calendar().scrollTo(0, fomWeek.offsetTop - headerHeight - weekHeaderHeight);
}

window.addEventListener('resize', function(event) {
  ScrollToFirstOfMonth(0);
  runTemps();
});


function runTemps() {
  $('.select-container').each(function(index, element) {
    // Get the parent and sibling elements
    var childrenArray = Array.from(this.parentElement.children);
    var thisIndex = childrenArray.indexOf(this);
    
    var thisIsFirst = thisIndex === 0;
    var thisIsLast = thisIndex === childrenArray.length - 1;

    // Calculate the height for each element
    var nextSiblingHeight = this.nextElementSibling ? $(this.nextElementSibling).outerHeight() : 0;
    var previousSiblingHeight = this.previousElementSibling ? $(this.previousElementSibling).outerHeight() : 0;

    var height = thisIsFirst ? nextSiblingHeight : (thisIsLast ? previousSiblingHeight : previousSiblingHeight);

    // Set the max-height based on calculated height
    $(this).css({
      'max-height': height + 'px'
    });
  });
  if (window.news) {
    window.news.resize()
  }
}

runTemps()


function REMOVE_EVENTS() {
  for (var key in events) {
    const [selector, eventType] = key.split(':');
    const elements = document.querySelectorAll(selector);

    elements.forEach(element => {
      element.removeEventListener(eventType, events[key]);
    });
  }
}

function ADD_EVENTS(events_to_add = events) {
  REMOVE_EVENTS();

  for (var key in events_to_add) {
    const [selector, eventType] = key.split(':');
    let elements;
    if (selector == 'window') {
      window.addEventListener(eventType, events_to_add[key]);
    } else {
      elements = document.querySelectorAll(selector);
      // console.log(elements.length, selector)
      elements.forEach(element => {
        // console.log('Adding ' + eventType + ' for ' + selector, element)
        element.addEventListener(eventType, events_to_add[key]);
      });
    }
  }
}

console.log(">>>")

ADD_EVENTS()

class News {
  constructor() {}
  load_the_news() {
    var url = `/api/${Page.DAILYNEWS}`
    var news = new XMLHttpRequest()
    news.open("GET", url)
    news.addEventListener('load', function() {
      var res = JSON.parse(this.response)
      if (res.status == 'success') {
        document.getElementById("right").innerHTML = res.template
        ADD_EVENTS()
      }
    })
    news.send()
  }
}

function initnews() {
  let news = new News()
  news.load_the_news()
}

function teardownnews() {
  document.getElementById('right').innerHTML = '<did class="loading">loading...</did>'
}






