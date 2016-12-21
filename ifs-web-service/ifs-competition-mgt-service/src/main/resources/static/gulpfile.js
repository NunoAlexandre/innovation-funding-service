// jshint ignore: start
var gulp = require('gulp')
var standard = require('gulp-standard')
var uglify = require('gulp-uglify')
var concat = require('gulp-concat')

gulp.task('default', ['js', 'css'])

// build all js
gulp.task('js', function () {
  return gulp.src([
    'js/ifsCompetitionManagementLoader.js',
    'js/ifs_modules/*.js',
    'js/ifs_pages/*.js'
  ])
  .pipe(standard())
  .pipe(standard.reporter('default', {
    breakOnError: false,
    breakOnWarning: false,
    quiet: true
  }))
  .pipe(concat('comp-management.min.js'))
  .pipe(uglify())
  .pipe(gulp.dest('js/dest'))
})
gulp.task('css', function () {})
gulp.task('css:watch', function () {})
