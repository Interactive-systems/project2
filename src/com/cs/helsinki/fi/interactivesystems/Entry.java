package com.cs.helsinki.fi.interactivesystems;

public class Entry {
	
	public final String job; //nimetus
	public final String isco; //isco //loomakasvatuse töötaja
	public final String ametValdkond; //ametValdkond //taimekasvatus, loomakasvatus, aiandus
	public final String tasks; //tooylesanded
	public final String requirements; //nouded
	public final String training; //omaltPooltPakume
	public final String applicationDate; //kandideerimiseKp
	public final String country; //riik
	public final String county; //maakond
	public final String locality; //asula
	public final String address; //aadress
	public final String employer; //tooandja
	public final String contact; //kontaktisik
	public final String email; //email
	public final String jobLength; //toosuhteKestus
	public final String experience; //tookogemus
	public final String hours; //tooaeg
	public final String salary; //tootasuTyyp
	public final String dateAdded; //lisamiseAeg
	public final String moreInfo; //lisainfo
	public final String phone; //telefon
	public final String education; //haridusTase
	
	public Entry(String job, String isco, String ametValdkond, String tasks,
			String requirements, String training, String applicationDate,
			String country, String county, String locality, String address,
			String employer, String contact, String email, String jobLength,
			String experience, String hours, String salary, String dateAdded,
			String moreInfo, String phone, String education) {
		
		this.job = job;
		this.isco = isco;
		this.ametValdkond = ametValdkond;
		this.tasks = tasks;
		this.requirements = requirements;
		this.training = training;
		this.applicationDate = applicationDate;
		this.country = country;
		this.county = county;
		this.locality = locality;
		this.address = address;
		this.employer = employer;
		this.contact = contact;
		this.email = email;
		this.jobLength = jobLength;
		this.experience = experience;
		this.hours = hours;
		this.salary = salary;
		this.dateAdded = dateAdded;
		this.moreInfo = moreInfo;
		this.phone = phone;
		this.education = education;
	}

	public String getJob() {
		return job;
	}

	public String getIsco() {
		return isco;
	}

	public String getAmetValdkond() {
		return ametValdkond;
	}

	public String getTasks() {
		return tasks;
	}

	public String getRequirements() {
		return requirements;
	}

	public String getTraining() {
		return training;
	}

	public String getApplicationDate() {
		return applicationDate;
	}

	public String getCountry() {
		return country;
	}

	public String getCounty() {
		return county;
	}

	public String getLocality() {
		return locality;
	}

	public String getAddress() {
		return address;
	}

	public String getEmployer() {
		return employer;
	}

	public String getContact() {
		return contact;
	}

	public String getEmail() {
		return email;
	}

	public String getJobLength() {
		return jobLength;
	}

	public String getExperience() {
		return experience;
	}

	public String getHours() {
		return hours;
	}

	public String getSalary() {
		return salary;
	}

	public String getDateAdded() {
		return dateAdded;
	}

	public String getMoreInfo() {
		return moreInfo;
	}

	public String getPhone() {
		return phone;
	}

	public String getEducation() {
		return education;
	}
}
