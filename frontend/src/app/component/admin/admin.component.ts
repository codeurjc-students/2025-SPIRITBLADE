import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../service/admin.service';
import { User } from '../../dto/user.dto';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  private adminService = inject(AdminService);

  users: User[] = [];
  searchTerm: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  
  // Pagination
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;
  
  // Filters
  filterRole: string = '';
  filterActive: string = 'all';  // 'all', 'true', 'false'
  
  // Modal state
  showCreateModal: boolean = false;
  showEditModal: boolean = false;
  showDeleteModal: boolean = false;
  
  // User being edited/deleted
  selectedUser: User | null = null;
  
  // New user form
  newUser: User = {
    id: 0,
    name: '',
    email: '',
    password: '',
    roles: ['USER'],
    active: true
  };

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    const filters: any = {};
    
    if (this.filterRole) {
      filters.role = this.filterRole;
    }
    
    if (this.filterActive !== 'all') {
      filters.active = this.filterActive === 'true';
    }
    
    if (this.searchTerm.trim()) {
      filters.search = this.searchTerm.trim();
    }
    
    this.adminService.getUsers(this.currentPage, this.pageSize, filters).subscribe({
      next: (response) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.errorMessage = 'Failed to load users. Please try again.';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;  // Reset to first page when applying filters
    this.loadUsers();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.filterRole = '';
    this.filterActive = 'all';
    this.currentPage = 0;
    this.loadUsers();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadUsers();
    }
  }

  get pages(): number[] {
    const maxPagesToShow = 5;
    const pages: number[] = [];
    
    let startPage = Math.max(0, this.currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxPagesToShow - 1);
    
    if (endPage - startPage < maxPagesToShow - 1) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  openCreateModal(): void {
    this.newUser = {
      id: 0,
      name: '',
      email: '',
      password: '',
      roles: ['USER'],
      active: true
    };
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  createUser(): void {
    if (!this.newUser.name || !this.newUser.email || !this.newUser.password) {
      alert('Please fill in all required fields');
      return;
    }

    this.adminService.createUser(this.newUser).subscribe({
      next: (user) => {
        console.log('User created:', user);
        this.loadUsers();
        this.closeCreateModal();
      },
      error: (error) => {
        console.error('Error creating user:', error);
        alert('Failed to create user. Username may already exist.');
      }
    });
  }

  openEditModal(user: User): void {
    this.selectedUser = { ...user };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedUser = null;
  }

  updateUser(): void {
    if (!this.selectedUser) {
      return;
    }

    this.adminService.updateUser(this.selectedUser.id, this.selectedUser).subscribe({
      next: () => {
        console.log('User updated');
        this.loadUsers();
        this.closeEditModal();
      },
      error: (error) => {
        console.error('Error updating user:', error);
        if (error.status === 403) {
          this.errorMessage = 'Cannot edit admin users. Admins can only manage regular users.';
        } else {
          this.errorMessage = 'Failed to update user.';
        }
        this.closeEditModal();
      }
    });
  }

  openDeleteModal(user: User): void {
    this.selectedUser = user;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.selectedUser = null;
  }

  deleteUser(): void {
    if (!this.selectedUser || !this.selectedUser.id) {
      return;
    }

    this.adminService.deleteUser(this.selectedUser.id).subscribe({
      next: () => {
        console.log('User deleted');
        this.loadUsers();
        this.closeDeleteModal();
      },
      error: (error) => {
        console.error('Error deleting user:', error);
        if (error.status === 403) {
          this.errorMessage = 'Cannot delete admin users. Admins can only manage regular users.';
        } else {
          this.errorMessage = 'Failed to delete user.';
        }
        this.closeDeleteModal();
      }
    });
  }

  toggleUserActive(user: User): void {
    if (!user.id) {
      return;
    }

    this.adminService.toggleUserActive(user.id).subscribe({
      next: (updatedUser) => {
        console.log('User active status toggled:', updatedUser);
        this.loadUsers();
      },
      error: (error) => {
        console.error('Error toggling user status:', error);
        if (error.status === 403) {
          this.errorMessage = 'Cannot modify admin users. Admins can only manage regular users.';
        } else {
          this.errorMessage = 'Failed to toggle user status.';
        }
      }
    });
  }

  getRoleBadgeClass(roles: string[]): string {
    if (roles?.includes('ADMIN')) {
      return 'badge-admin';
    }
    return 'badge-user';
  }

  getStatusBadgeClass(active: boolean): string {
    return active ? 'badge-active' : 'badge-inactive';
  }

  /**
   * Check if a user has admin role.
   * Admins cannot take actions on other admins.
   */
  isAdmin(user: User): boolean {
    return user.roles?.includes('ADMIN') || user.roles?.includes('ROLE_ADMIN') || false;
  }
}
