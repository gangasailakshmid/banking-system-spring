"""Exercise login, CSRF, both registration flows, edit/delete and logout on local databases.
Run while ./run.sh is active. Uses .env credentials; deletes only its synthetic registrations.
"""
from pathlib import Path
import http.cookiejar
import re
import shlex
import urllib.error
import urllib.parse
import urllib.request
import uuid
import os
import subprocess

root = Path(__file__).resolve().parents[1]
config = dict(os.environ)
for line in (root / '.env').read_text().splitlines():
    if line.strip() and not line.startswith('#'):
        k, v = line.split('=', 1)
        config[k] = shlex.split(v)[0] if v else ''
base = 'http://localhost:' + config.get('PORT', '8081')
jar = http.cookiejar.CookieJar()
client = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))


def request(path, data=None):
    payload = None if data is None else urllib.parse.urlencode(data).encode()
    try:
        response = client.open(base + path, payload)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        return response.status, response.geturl(), response.read().decode()


def token(page):
    return re.search(r'name="_csrf"[^>]*value="([^"]+)"', page).group(1)


def command(args, env=None):
    return subprocess.check_output(args, env=env, text=True).strip()


status, url, page = request('/')
assert status == 200 and url.endswith('/login')
assert any(c.name == 'BANKING_SPRING_SESSION' for c in jar)
status, url, page = request('/login', dict(username=config['ADMIN_USERNAME'], password='wrong-password', _csrf=token(page)))
assert url.endswith('/login?error') and 'Invalid username or password' in page
status, url, page = request('/login', dict(username=config['ADMIN_USERNAME'], password=config['ADMIN_PASSWORD'], _csrf=token(page)))
assert url == base + '/' and 'Banking System Spring' in page
print('Login protection, failed login and successful login: passed')
status, _, blocked = request('/users/save', dict(firstName='MustNotSave'))
assert status == 403 and 'No action was taken' in blocked
print('Missing CSRF token is rejected with a readable page: passed')
_, _, page = request('/')
ids = []
marker = 'SpringCheck' + uuid.uuid4().hex[:10]
form = dict(firstName=marker, middleName='', lastName='Synthetic', gender='Other', address='123 Example St', city='City', state='CA', country='US', phone='5550100', bankName='Test Bank', accountNumber='001234', ssn='000-00-0001')


def created_id(html, name):
    row = next(row for row in re.findall(r'<tr data-user-row>(.*?)</tr>', html, re.S) if name in row)
    assert re.search(r'action="/users/\d+/delete"[^>]*>\s*<input type="hidden" name="_csrf"', row)
    return re.search(r'/users/(\d+)/edit', row).group(1)


try:
    status, _, page = request('/users/save', dict(form, _csrf=token(page)))
    assert status == 200 and 'User registered successfully.' in page
    registration_id = created_id(page, marker)
    ids.append(registration_id)
    mysql_env = dict(os.environ, MYSQL_PWD=config['MYSQL_PASSWORD'])
    pg_env = dict(os.environ, PGPASSWORD=config['POSTGRES_PASSWORD'])
    assert command(['mysql', '-u', config['MYSQL_USERNAME'], '-N', 'banking_spring_personal', '-e', 'SELECT firstName FROM personal_details WHERE id=' + registration_id], mysql_env) == marker
    assert command(['psql', '-h', 'localhost', '-U', config['POSTGRES_USERNAME'], '-d', 'banking_spring_bank', '-tAc', 'SELECT accountNumber FROM bank_details WHERE id=' + registration_id], pg_env) == '001234'
    mongo_uri = config.get('MONGO_URI', 'mongodb://127.0.0.1:27017/?serverSelectionTimeoutMS=5000')
    assert command(['mongosh', mongo_uri, '--quiet', '--eval', 'db.getSiblingDB("banking_spring_address").address_details.findOne({_id:NumberLong("' + registration_id + '")}).city']) == 'City'
    print('Registration persisted in all three local stores: passed')
    _, _, page = request('/users/' + registration_id + '/edit')
    status, _, page = request('/users/save', dict(form, editId=registration_id, firstName=marker+'Edited', accountNumber='', ssn='', _csrf=token(page)))
    assert status == 200 and 'User updated successfully.' in page and marker+'Edited' in page
    assert command(['psql', '-h', 'localhost', '-U', config['POSTGRES_USERNAME'], '-d', 'banking_spring_bank', '-tAc', "SELECT accountNumber || ':' || ssn FROM bank_details WHERE id=" + registration_id], pg_env) == '001234:000000001'
    print('Editing preserves blank account number and SSN: passed')
    _, _, page = request('/register/new')
    status, _, page = request('/submit', dict(firstName=marker+'Wizard', lastName='Synthetic', gender='Other', _csrf=token(page)))
    assert status == 200 and 'Address and Contact Details' in page
    status, _, page = request('/submit-address', dict(address='Street', city='City', state='CA', country='US', phone='555', _csrf=token(page)))
    assert status == 200 and 'Bank Details' in page
    status, _, page = request('/register', dict(bankName='Bank', accountNumber='00123', ssn='000000001', _csrf=token(page)))
    assert status == 200 and 'successfully registered' in page
    _, _, page = request('/')
    ids.append(created_id(page, marker+'Wizard'))
    print('Original three-page registration flow: passed')
finally:
    for registration_id in ids:
        _, _, page = request('/')
        status, _, page = request('/users/' + registration_id + '/delete', dict(_csrf=token(page)))
        assert status == 200 and 'User deleted successfully.' in page
print('Synthetic registrations deleted: passed')
_, _, page = request('/')
status, url, page = request('/logout', dict(_csrf=token(page)))
assert status == 200 and url.endswith('/login?logout')
assert request('/')[1].endswith('/login')
print('Logout invalidates login and protects the dashboard again: passed')
