import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DashboardService } from './dashboard.service';
import { AuthService } from './auth.service';
import { DashboardData } from '../models/dashboard.model';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;
  let authServiceSpy: any;

  beforeEach(() => {
    authServiceSpy = {
      getAuthHeader: vi.fn().mockReturnValue('encoded-auth')
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        DashboardService,
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch dashboard data', () => {
    const mockData: DashboardData = {
      summary: {
        openTasks: 5,
        openTasksDelta: 1,
        activeUsers: 10,
        activeUsersDelta: 0,
        completedTasks: 20,
        completedTasksDelta: 2,
        todayLogs: 50,
        todayLogsDelta: 5
      },
      priorityTasks: [],
      recentActivity: [],
      recentUsers: [],
      taskDistribution: {
        open: 5,
        inProgress: 0,
        completed: 20,
        total: 25
      }
    };

    service.getDashboardData().subscribe(data => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne('/api/dashboard');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Basic encoded-auth');
    req.flush(mockData);
  });
});
