import { TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { MarkdownToHtmlPipe } from './markdown-to-html.pipe';

describe('MarkdownToHtmlPipe', () => {
  let pipe: MarkdownToHtmlPipe;
  let sanitizer: jasmine.SpyObj<DomSanitizer>;

  beforeEach(() => {
    const sanitizerSpy = jasmine.createSpyObj('DomSanitizer', ['bypassSecurityTrustHtml']);
    sanitizerSpy.bypassSecurityTrustHtml.and.callFake((html: string) => html as any);

    TestBed.configureTestingModule({
      providers: [
        { provide: DomSanitizer, useValue: sanitizerSpy }
      ]
    });
    sanitizer = TestBed.inject(DomSanitizer) as jasmine.SpyObj<DomSanitizer>;
    pipe = new MarkdownToHtmlPipe(sanitizer);
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return empty string for null input', () => {
    expect(pipe.transform(null)).toBe('');
  });

  it('should return empty string for undefined input', () => {
    expect(pipe.transform(undefined as any)).toBe('');
  });

  it('should convert headers', () => {
    const input = '# Header 1\n## Header 2\n### Header 3';
    const result = pipe.transform(input);
    expect(result).toContain('<h1>Header 1</h1>');
    expect(result).toContain('<h2>Header 2</h2>');
    expect(result).toContain('<h3>Header 3</h3>');
  });

  it('should convert bold text', () => {
    const input = '**bold text**';
    const result = pipe.transform(input);
    expect(result).toContain('<strong>bold text</strong>');
  });

  it('should convert italic text', () => {
    const input = '*italic text*';
    const result = pipe.transform(input);
    expect(result).toContain('<em>italic text</em>');
  });

  it('should convert unordered lists with dashes', () => {
    const input = '- Item 1\n- Item 2';
    const result = pipe.transform(input);
    expect(result).toContain('<ul><li>Item 1</li><li>Item 2</li></ul>');
  });

  it('should convert unordered lists with asterisks', () => {
    const input = '* Item 1\n* Item 2';
    const result = pipe.transform(input);
    expect(result).toContain('<ul><li>Item 1</li><li>Item 2</li></ul>');
  });

  it('should convert paragraphs', () => {
    const input = 'First paragraph\n\nSecond paragraph';
    const result = pipe.transform(input);
    expect(result).toContain('<p>First paragraph</p><p>Second paragraph</p>');
  });

  it('should convert line breaks', () => {
    const input = 'Line 1\nLine 2';
    const result = pipe.transform(input);
    expect(result).toContain('Line 1<br>Line 2');
  });

  it('should handle complex markdown', () => {
    const input = '# Title\n\nThis is **bold** and *italic*.\n\n- List item 1\n- List item 2';
    const result = pipe.transform(input);
    expect(result).toContain('<h1>Title</h1>');
    expect(result).toContain('<strong>bold</strong>');
    expect(result).toContain('<em>italic</em>');
    expect(result).toContain('<ul><li>List item 1</li><li>List item 2</li></ul>');
  });

  it('should sanitize HTML output', () => {
    const input = '**test**';
    const result = pipe.transform(input);
    expect(sanitizer.bypassSecurityTrustHtml).toHaveBeenCalled();
  });
});