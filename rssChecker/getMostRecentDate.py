import feedparser

def getMostRecentDate(url):
	f = feedparser.parse(url);
	y = f.entries[0].updated_parsed(0)
	m = f.entries[0].updated_parsed(1)
	d = f.entries[0].updated_parsed(2)

	return y, m, d

url = input()
getMostRecentDate(url)
