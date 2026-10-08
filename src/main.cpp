#include <iostream>

#include <limits>

#include <chrono>
#include <ctime>
#include <iomanip>
#include <cmath>

#include <fstream>
#include <string>

#include <map>
#include <vector>

std::multimap<int, std::string> lines;

inline bool localtime_portable(const std::time_t* t, std::tm* out) {
#ifdef _WIN32
    return localtime_s(out, t) == 0;     // Windows: (tm*, time_t*), devuelve errno_t
#else
    return localtime_r(t, out) != nullptr; // POSIX: (time_t*, tm*), devuelve tm*
#endif
}

std::vector<std::string> split(std::string s, char delimiter)
{
    std::vector<std::string> result;
    std::stringstream ss(s);
    std::string item;

    while (std::getline(ss, item, delimiter))
    {
        result.push_back(item);
    }

    return result;
}

void processFile(const std::string& path)
{
    std::ifstream file(path);

    if (!file.is_open())
    {
        std::cerr << "Error, the file could not be opened...\n";
        return;
    }

    std::string line;
    
    auto now = std::chrono::system_clock::now();
    std::time_t time = std::chrono::system_clock::to_time_t(now);

    std::tm date;
    localtime_portable(&time, &date);

    int day = date.tm_mday;
    int month = date.tm_mon + 1;
    int year = date.tm_year + 1900;
    
    std::chrono::year_month_day today {
		std::chrono::year{year},
		std::chrono::month{static_cast<unsigned>(month)},
		std::chrono::day{static_cast<unsigned>(day)}
	};
	
	std::chrono::sys_days dayToday{today};
	
	lines.clear();

    while (std::getline(file, line))
    {
		std::string name = split(line, ';')[0];
		std::string strDate = split(line, ';')[1];
        int nDay = std::stoi(split(strDate, '/')[0]);
        int nMonth = std::stoi(split(strDate, '/')[1]);
        int nYear = std::stoi(split(strDate, '/')[2]);
        
        std::chrono::year_month_day daux{
			std::chrono::year{year},
			std::chrono::month{static_cast<unsigned>(nMonth)},
			std::chrono::day{static_cast<unsigned>(nDay)}
		};
		
		std::chrono::sys_days dayAux{daux};
		auto difference = (dayToday - dayAux).count();
		int daysRemaining;
		if(difference < 0) { // The birthday is coming up this year
			daysRemaining = (dayAux - dayToday).count();			
		} else { // The birthday has already passed this year
			daux = std::chrono::year_month_day{std::chrono::year{daux.year() + std::chrono::years{1}}, daux.month(), daux.day()};
			std::chrono::sys_days dayAux2{daux};
			daysRemaining = (dayAux2 - dayToday).count();
		}
		
		std::chrono::year_month_day birth {
			std::chrono::year{nYear},
			std::chrono::month{static_cast<unsigned>(nMonth)},
			std::chrono::day{static_cast<unsigned>(nDay)}
		};
		
		std::chrono::sys_days dayBirth{birth};
		
		int yo = static_cast<int>(std::ceil(static_cast<float>((dayToday - dayBirth).count()) / 365.25));
		
		std::string line = "";
		
		if(daysRemaining == 365) {
			daysRemaining = 0;
		}
		
		if(daysRemaining == 0) {
			line += "Today ";
		} else if(daysRemaining == 1) {
			line += "In 1 day ";
		} else {
			line += "In " + std::to_string(daysRemaining) + " days ";
		}
		
		line += name + " will turn " + std::to_string(yo) + ", (" + std::to_string(nDay) + "/" + std::to_string(nMonth) + "/" + std::to_string(nYear) + ")\n\n";
		
		lines.insert({daysRemaining, line});
    }
    
    std::cout << "Current date: " << today.day() << "/" << today.month() << "/" << today.year() << "\n\n";
    
    for (const auto& [number, text] : lines) {
		std::cout << text;
	}
    
}

int main() {
	
    processFile("dates.txt");
    
    std::cout << "\nPress Enter to exit...";
    std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
    
    return 0;
    
}
