package peter.finance_calendar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import peter.finance_calendar.models.ControllerResponse;
import peter.finance_calendar.models.SyncData;

@RestController
public class CalendarController {
    
    @GetMapping("/sync-data")
    public ResponseEntity<ControllerResponse<SyncData>> syncData() {
        SyncData syncData = new SyncData();
        ControllerResponse<SyncData> res = new ControllerResponse<>("success");
        res.setData(syncData);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
