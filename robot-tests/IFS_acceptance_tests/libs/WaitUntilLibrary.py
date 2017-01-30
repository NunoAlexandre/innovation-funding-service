from logging import warn
from robot.libraries.BuiltIn import BuiltIn
s2l = BuiltIn().get_library_instance('Selenium2Library')

currently_waiting_for_keyword_to_succeed = False



# a decorator that sets and unsets a special flag when performing "Wait until" keywords and enforces that the
# screenshots are only taken when failure results from a genuine test failure
def setting_wait_until_flag(func):

  def decorator(*args):

    global currently_waiting_for_keyword_to_succeed

    currently_waiting_for_keyword_to_succeed = True
    try:
      result = func(*args)
    except:
      capture_large_screenshot()
      raise
    finally:
      currently_waiting_for_keyword_to_succeed = False
    return result

  return decorator


@setting_wait_until_flag
def wait_until_keyword_succeeds_without_screenshots(retry, retry_interval, keyword, *args):
  return BuiltIn().wait_until_keyword_succeeds(retry, retry_interval, keyword, *args)


@setting_wait_until_flag
def wait_until_element_is_visible_without_screenshots(locator, timeout=None, error=None):
  return s2l.wait_until_element_is_visible(locator, timeout, error)


@setting_wait_until_flag
def wait_until_element_is_not_visible_without_screenshots(locator, timeout=None, error=None):
  return s2l.wait_until_element_is_not_visible(locator, timeout, error)


@setting_wait_until_flag
def wait_until_page_contains_without_screenshots(text, timeout=None, error=None):
  return s2l.wait_until_page_contains(text, timeout, error)


@setting_wait_until_flag
def wait_until_page_contains_element_without_screenshots(locator, timeout=None, error=None):
  return s2l.wait_until_page_contains_element(locator, timeout, error)


@setting_wait_until_flag
def wait_until_page_does_not_contain_without_screenshots(text, timeout=None, error=None):
  return s2l.wait_until_page_does_not_contain(text, timeout, error)


@setting_wait_until_flag
def wait_until_element_contains_without_screenshots(locator, text, timeout=None, error=None):
  return s2l.wait_until_element_contains(locator, text, timeout, error)


@setting_wait_until_flag
def wait_until_element_does_not_contain_without_screenshots(locator, text, timeout=None, error=None):
  return s2l.wait_until_element_does_not_contain(locator, text, timeout, error)


@setting_wait_until_flag
def wait_until_element_is_enabled_without_screenshots(locator, timeout=None, error=None):
  return s2l.wait_until_element_is_enabled(locator, timeout, error)


@setting_wait_until_flag
def run_keyword_and_ignore_error_without_screenshots(keyword, *args):
  return BuiltIn().run_keyword_and_ignore_error(keyword, *args)


@setting_wait_until_flag
def run_keyword_and_return_status_without_screenshots(keyword, *args):
  return BuiltIn().run_keyword_and_return_status(keyword, *args)


def capture_page_screenshot_on_failure():
  if not currently_waiting_for_keyword_to_succeed:
    capture_large_screenshot()


def capture_large_screenshot():

  page_height = s2l._current_browser().execute_script("return $(document).height();")

  original_height = s2l.get_window_size()[1]

  s2l.set_window_size(1920, page_height)
  warn("Capturing a screenshot")
  s2l.capture_page_screenshot()
  s2l.set_window_size(1920, original_height)