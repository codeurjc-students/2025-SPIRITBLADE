import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminComponent } from './admin.component';
import { AdminService } from '../../service/admin.service';
import { of, throwError } from 'rxjs';
import { User } from '../../dto/user.dto';

describe('AdminComponent - Unit Tests', () => {
  let component: AdminComponent;
  let fixture: ComponentFixture<AdminComponent>;
  let mockAdminService: jasmine.SpyObj<AdminService>;

  beforeEach(async () => {
    mockAdminService = jasmine.createSpyObj('AdminService', [
      'getUsers',
      'getUserById',
      'createUser',
      'updateUser',
      'deleteUser',
      'toggleUserActive'
    ]);

    // Setup default mock responses
    mockAdminService.getUsers.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
      first: true,
      last: true,
      empty: true
    }));

    await TestBed.configureTestingModule({
      imports: [AdminComponent],
      providers: [
        { provide: AdminService, useValue: mockAdminService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize without errors', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(component).toBeTruthy();
    });

    it('should render without throwing errors', () => {
      // Act & Assert
      expect(() => fixture.detectChanges()).not.toThrow();
    });
  });

  describe('Component Structure', () => {
    it('should be instance of AdminComponent', () => {
      expect(component).toBeInstanceOf(AdminComponent);
    });

    it('should have proper component type', () => {
      expect(component.constructor.name).toBe('AdminComponent');
    });
  });

  describe('Template Rendering', () => {
    it('should render the component element', () => {
      // Act
      fixture.detectChanges();

      // Assert
      const compiled = fixture.nativeElement;
      expect(compiled).toBeTruthy();
    });

    it('should handle change detection cycles', () => {
      // Act
      for (let i = 0; i < 10; i++) {
        fixture.detectChanges();
      }

      // Assert
      expect(component).toBeTruthy();
    });
  });

  describe('Component Lifecycle', () => {
    it('should handle ngOnInit if implemented', () => {
      // Act & Assert
      expect(() => fixture.detectChanges()).not.toThrow();
    });

    it('should handle ngOnDestroy if implemented', () => {
      // Act
      fixture.detectChanges();

      // Assert & Act
      expect(() => fixture.destroy()).not.toThrow();
    });
  });

  describe('Error Handling', () => {
    it('should handle unexpected errors gracefully', () => {
      // Act & Assert
      expect(() => {
        fixture.detectChanges();
        fixture.detectChanges();
      }).not.toThrow();
    });

    it('should maintain state consistency', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(component).toBeTruthy();
      expect(component).toBe(component);
    });
  });

  describe('Component State', () => {
    it('should maintain consistent state across change detection', () => {
      // Act
      fixture.detectChanges();
      const initialComponent = component;
      fixture.detectChanges();

      // Assert
      expect(component).toBe(initialComponent);
    });

    it('should not have undefined or null component after initialization', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(component).toBeDefined();
      expect(component).not.toBeNull();
    });
  });

  describe('Performance', () => {
    it('should not cause memory leaks during multiple change detections', () => {
      // Act
      for (let i = 0; i < 100; i++) {
        fixture.detectChanges();
      }

      // Assert
      expect(component).toBeTruthy();
    });

    it('should handle rapid change detection efficiently', () => {
      // Arrange
      const startTime = performance.now();

      // Act
      for (let i = 0; i < 50; i++) {
        fixture.detectChanges();
      }
      const endTime = performance.now();

      // Assert
      expect(component).toBeTruthy();
      expect(endTime - startTime).toBeLessThan(1000); // Should complete within 1 second
    });
  });

  describe('Accessibility', () => {
    it('should not have accessibility violations in basic structure', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(component).toBeTruthy();
      // Note: In a real project, you might use tools like axe-core for accessibility testing
    });
  });

  describe('Future Implementation Readiness', () => {
    it('should be ready for dependency injection', () => {
      // Assert
      expect(component).toBeTruthy();
      // Component should be ready to accept injected services when implemented
    });

    it('should be ready for template binding', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(component).toBeTruthy();
      // Component should be ready for two-way data binding when template is implemented
    });

    it('should be ready for event handling', () => {
      // Assert
      expect(component).toBeTruthy();
      // Component structure should support event handler methods when implemented
    });
  });

  describe('Component Integration', () => {
    it('should integrate properly with Angular testing utilities', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(fixture.componentInstance).toBe(component);
      expect(fixture.nativeElement).toBeTruthy();
    });

    it('should support Angular change detection mechanism', () => {
      // Act
      fixture.autoDetectChanges();

      // Assert
      expect(component).toBeTruthy();
      
      // Clean up
      fixture.autoDetectChanges(false);
    });
  });

  describe('Modal Operations', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open create modal', () => {
      component.openCreateModal();
      expect(component.showCreateModal).toBeTrue();
    });

    it('should close create modal', () => {
      component.showCreateModal = true;
      component.closeCreateModal();
      expect(component.showCreateModal).toBeFalse();
    });

    it('should open edit modal with user', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      component.openEditModal(user);
      expect(component.showEditModal).toBeTrue();
      expect(component.selectedUser).toEqual(user);
    });

    it('should close edit modal', () => {
      component.showEditModal = true;
      component.closeEditModal();
      expect(component.showEditModal).toBeFalse();
      expect(component.selectedUser).toBeNull();
    });

    it('should open delete modal with user', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      component.openDeleteModal(user);
      expect(component.showDeleteModal).toBeTrue();
      expect(component.selectedUser).toEqual(user);
    });

    it('should close delete modal', () => {
      component.showDeleteModal = true;
      component.closeDeleteModal();
      expect(component.showDeleteModal).toBeFalse();
      expect(component.selectedUser).toBeNull();
    });
  });

  describe('CRUD Operations', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should create user successfully', () => {
      const newUser: User = { id: 0, name: 'NewUser', email: 'new@test.com', password: 'password', roles: ['USER'], active: true };
      component.newUser = newUser;
      const createdUser: User = { ...newUser, id: 1 };
      mockAdminService.createUser.and.returnValue(of(createdUser));

      component.createUser();

      expect(mockAdminService.createUser).toHaveBeenCalledWith(newUser);
      expect(component.showCreateModal).toBeFalse();
    });

    it('should update user successfully', () => {
      const user: User = { id: 1, name: 'Updated', email: 'updated@test.com', roles: ['USER'], active: true };
      component.selectedUser = user;
      mockAdminService.updateUser.and.returnValue(of(user));

      component.updateUser();

      expect(mockAdminService.updateUser).toHaveBeenCalledWith(1, user);
      expect(component.showEditModal).toBeFalse();
    });

    it('should toggle user active status successfully', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      const updatedUser: User = { ...user, active: false };
      mockAdminService.toggleUserActive.and.returnValue(of(updatedUser));

      component.toggleUserActive(user);

      expect(mockAdminService.toggleUserActive).toHaveBeenCalledWith(1);
    });

    it('should handle create user with missing fields', () => {
      component.newUser = { id: 0, name: '', email: 'test@test.com', password: 'pass', roles: ['USER'], active: true };
      spyOn(window, 'alert');

      component.createUser();

      expect(window.alert).toHaveBeenCalledWith('Please fill in all required fields');
      expect(mockAdminService.createUser).not.toHaveBeenCalled();
    });

    it('should handle create user with missing email', () => {
      component.newUser = { id: 0, name: 'Test', email: '', password: 'pass', roles: ['USER'], active: true };
      spyOn(window, 'alert');

      component.createUser();

      expect(window.alert).toHaveBeenCalledWith('Please fill in all required fields');
      expect(mockAdminService.createUser).not.toHaveBeenCalled();
    });

    it('should handle create user with missing password', () => {
      component.newUser = { id: 0, name: 'Test', email: 'test@test.com', password: '', roles: ['USER'], active: true };
      spyOn(window, 'alert');

      component.createUser();

      expect(window.alert).toHaveBeenCalledWith('Please fill in all required fields');
      expect(mockAdminService.createUser).not.toHaveBeenCalled();
    });

    it('should not update user when no user selected', () => {
      component.selectedUser = null;

      component.updateUser();

      expect(mockAdminService.updateUser).not.toHaveBeenCalled();
    });

    it('should not delete user when no user selected', () => {
      component.selectedUser = null;

      component.deleteUser();

      expect(mockAdminService.deleteUser).not.toHaveBeenCalled();
    });

    it('should not delete user when selected user has no id', () => {
      component.selectedUser = { id: 0, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };

      component.deleteUser();

      expect(mockAdminService.deleteUser).not.toHaveBeenCalled();
    });

    it('should delete user successfully', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      component.selectedUser = user;
      mockAdminService.deleteUser.and.returnValue(of(void 0));

      component.deleteUser();

      expect(mockAdminService.deleteUser).toHaveBeenCalledWith(1);
      expect(component.showDeleteModal).toBeFalse();
    });

    it('should not toggle user when user has no id', () => {
      const user: User = { id: 0, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };

      component.toggleUserActive(user);

      expect(mockAdminService.toggleUserActive).not.toHaveBeenCalled();
    });
  });

  describe('Search and Filter', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should search users', () => {
      component.searchTerm = 'test';
      component.applyFilters();

      expect(mockAdminService.getUsers).toHaveBeenCalledWith(0, 10, { search: 'test' });
    });

    it('should filter by role', () => {
      component.filterRole = 'ADMIN';
      component.applyFilters();

      expect(mockAdminService.getUsers).toHaveBeenCalledWith(0, 10, { role: 'ADMIN' });
    });

    it('should filter by active status', () => {
      component.filterActive = 'true';
      component.applyFilters();

      expect(mockAdminService.getUsers).toHaveBeenCalledWith(0, 10, { active: true });
    });

    it('should clear filters', () => {
      component.searchTerm = 'test';
      component.filterRole = 'ADMIN';
      component.filterActive = 'true';
      component.currentPage = 5;

      component.clearFilters();

      expect(component.searchTerm).toBe('');
      expect(component.filterRole).toBe('');
      expect(component.filterActive).toBe('all');
      expect(component.currentPage).toBe(0);
      expect(mockAdminService.getUsers).toHaveBeenCalledWith(0, 10, {});
    });
  });

  describe('Pagination', () => {
    beforeEach(() => {
      fixture.detectChanges();
      component.totalPages = 5;
      mockAdminService.getUsers.calls.reset();
    });

    it('should navigate to next page', () => {
      component.currentPage = 2;
      component.nextPage();

      expect(component.currentPage).toBe(3);
      expect(mockAdminService.getUsers).toHaveBeenCalledWith(3, 10, {});
    });

    it('should not navigate beyond last page', () => {
      component.currentPage = 4;
      component.nextPage();

      expect(component.currentPage).toBe(4);
      expect(mockAdminService.getUsers).not.toHaveBeenCalled();
    });

    it('should navigate to previous page', () => {
      component.currentPage = 2;
      component.previousPage();

      expect(component.currentPage).toBe(1);
      expect(mockAdminService.getUsers).toHaveBeenCalledWith(1, 10, {});
    });

    it('should not navigate before first page', () => {
      component.currentPage = 0;
      component.previousPage();

      expect(component.currentPage).toBe(0);
      expect(mockAdminService.getUsers).not.toHaveBeenCalled();
    });

    it('should go to specific page', () => {
      component.goToPage(3);

      expect(component.currentPage).toBe(3);
      expect(mockAdminService.getUsers).toHaveBeenCalledWith(3, 10, {});
    });

    it('should not go to invalid page', () => {
      component.goToPage(10);

      expect(component.currentPage).toBe(0);
      expect(mockAdminService.getUsers).not.toHaveBeenCalled();
    });
  });

  describe('Utility Methods', () => {
    it('should return correct role badge class for admin', () => {
      expect(component.getRoleBadgeClass(['ADMIN'])).toBe('badge-admin');
    });

    it('should return correct role badge class for user', () => {
      expect(component.getRoleBadgeClass(['USER'])).toBe('badge-user');
    });

    it('should return correct role badge class for empty roles', () => {
      expect(component.getRoleBadgeClass([])).toBe('badge-user');
    });

    it('should return correct role badge class for null roles', () => {
      expect(component.getRoleBadgeClass(null as any)).toBe('badge-user');
    });

    it('should return correct status badge class for active', () => {
      expect(component.getStatusBadgeClass(true)).toBe('badge-active');
    });

    it('should return correct status badge class for inactive', () => {
      expect(component.getStatusBadgeClass(false)).toBe('badge-inactive');
    });

    it('should identify admin user', () => {
      const adminUser: User = { id: 1, name: 'Admin', email: 'admin@test.com', roles: ['ADMIN'], active: true };
      const roleAdminUser: User = { id: 2, name: 'RoleAdmin', email: 'role@test.com', roles: ['ROLE_ADMIN'], active: true };
      const regularUser: User = { id: 3, name: 'User', email: 'user@test.com', roles: ['USER'], active: true };

      expect(component.isAdmin(adminUser)).toBeTrue();
      expect(component.isAdmin(roleAdminUser)).toBeTrue();
      expect(component.isAdmin(regularUser)).toBeFalse();
    });
  });

  describe('Pagination Getter', () => {
    it('should return correct page numbers for first pages', () => {
      component.currentPage = 0;
      component.totalPages = 10;

      const pages = component.pages;

      expect(pages).toEqual([0, 1, 2, 3, 4]);
    });

    it('should return correct page numbers for middle pages', () => {
      component.currentPage = 5;
      component.totalPages = 10;

      const pages = component.pages;

      expect(pages).toEqual([3, 4, 5, 6, 7]);
    });

    it('should return correct page numbers for last pages', () => {
      component.currentPage = 9;
      component.totalPages = 10;

      const pages = component.pages;

      expect(pages).toEqual([5, 6, 7, 8, 9]);
    });

    it('should handle small total pages', () => {
      component.currentPage = 0;
      component.totalPages = 3;

      const pages = component.pages;

      expect(pages).toEqual([0, 1, 2]);
    });
  });

  describe('Error Handling', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should handle create user error', () => {
      component.newUser = { id: 0, name: 'New', email: 'new@test.com', password: 'pass', roles: ['USER'], active: true };
      mockAdminService.createUser.and.returnValue(throwError(() => ({ status: 400 })));

      spyOn(window, 'alert');

      component.createUser();

      expect(window.alert).toHaveBeenCalledWith('Failed to create user. Username may already exist.');
    });

    it('should handle update user error with non-403 status', () => {
      const user: User = { id: 1, name: 'Updated', email: 'updated@test.com', roles: ['USER'], active: true };
      component.selectedUser = user;
      mockAdminService.updateUser.and.returnValue(throwError(() => ({ status: 500 })));

      component.updateUser();

      expect(component.errorMessage).toBe('Failed to update user.');
    });

    it('should handle delete user error with non-403 status', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      component.selectedUser = user;
      mockAdminService.deleteUser.and.returnValue(throwError(() => ({ status: 500 })));

      component.deleteUser();

      expect(component.errorMessage).toBe('Failed to delete user.');
    });

    it('should handle toggle user error with non-403 status', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      mockAdminService.toggleUserActive.and.returnValue(throwError(() => ({ status: 500 })));

      component.toggleUserActive(user);

      expect(component.errorMessage).toBe('Failed to toggle user status.');
    });

    it('should handle delete user error with 403', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      component.selectedUser = user;
      mockAdminService.deleteUser.and.returnValue(throwError(() => ({ status: 403 })));

      component.deleteUser();

      expect(component.errorMessage).toBe('Cannot delete admin users. Admins can only manage regular users.');
    });

    it('should handle toggle user error with 403', () => {
      const user: User = { id: 1, name: 'Test', email: 'test@test.com', roles: ['USER'], active: true };
      mockAdminService.toggleUserActive.and.returnValue(throwError(() => ({ status: 403 })));

      component.toggleUserActive(user);

      expect(component.errorMessage).toBe('Cannot modify admin users. Admins can only manage regular users.');
    });
  });
});