import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskItemComponent } from './task-item.component';
import { CommonModule } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { TranslateModule } from '@ngx-translate/core';
import { TaskStatus, Priority } from '../../models/task.model';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('TaskItemComponent', () => {
  let component: TaskItemComponent;
  let fixture: ComponentFixture<TaskItemComponent>;

  const mockTask = {
    id: 1,
    title: 'Test Task',
    description: 'Test Desc',
    status: TaskStatus.OPEN,
    priority: Priority.MEDIUM,
    dueDate: '2026-01-01',
    tags: ['tag1']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TaskItemComponent,
        CommonModule,
        MatMenuModule,
        MatCheckboxModule,
        MatIconModule,
        MatChipsModule,
        TranslateModule.forRoot()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskItemComponent);
    component = fixture.componentInstance;
    component.task = mockTask;
    component.formattedDate = 'Jan 1, 2026';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit toggleDone', () => {
    const spy = vi.spyOn(component.toggleDone, 'emit');
    const event = new MouseEvent('click');
    component.onToggleDone(event);
    expect(spy).toHaveBeenCalled();
  });

  it('should emit edit', () => {
    const spy = vi.spyOn(component.edit, 'emit');
    const event = new MouseEvent('click');
    component.onEdit(event);
    expect(spy).toHaveBeenCalled();
  });

  it('should emit delete', () => {
    const spy = vi.spyOn(component.delete, 'emit');
    const event = new MouseEvent('click');
    component.onDelete(event);
    expect(spy).toHaveBeenCalled();
  });

  it('should emit toggleSelection', () => {
    const spy = vi.spyOn(component.toggleSelection, 'emit');
    component.onToggleSelection();
    expect(spy).toHaveBeenCalled();
  });

  it('should emit startEditTitle', () => {
    const spy = vi.spyOn(component.startEditTitle, 'emit');
    component.onStartEditTitle();
    expect(spy).toHaveBeenCalled();
  });

  it('should emit saveTitle', () => {
    const spy = vi.spyOn(component.saveTitle, 'emit');
    component.onSaveTitle('New Title');
    expect(spy).toHaveBeenCalledWith('New Title');
  });

  it('should emit setPriority', () => {
    const spy = vi.spyOn(component.setPriority, 'emit');
    component.onSetPriority(Priority.HIGH);
    expect(spy).toHaveBeenCalledWith(Priority.HIGH);
  });

  it('should emit cycleStatus', () => {
    const spy = vi.spyOn(component.cycleStatus, 'emit');
    const event = new MouseEvent('click');
    component.onCycleStatus(event);
    expect(spy).toHaveBeenCalledWith(event);
  });

  it('should emit toggleActions', () => {
    const spy = vi.spyOn(component.toggleActions, 'emit');
    const event = new MouseEvent('click');
    component.onToggleActions(event);
    expect(spy).toHaveBeenCalledWith(event);
  });
});
