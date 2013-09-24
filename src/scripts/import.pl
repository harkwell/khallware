#!/usr/bin/perl
# import.pl

use File::Find;                       # traverse filesystem
use File::Basename;                   # return basename of file
use Data::Dumper;                     # to help debuggin data structures

use constant TRUE      => 1;
use constant FALSE     => '';
use constant WEBHOST   => 'tomcat-server';
use constant AUDIODIR  => '/usr/local/music/';
use constant PHOTODIR  => '/home/guest/media/photo/';
#use constant AUDIODIR  => '/home/guest/tmp/oggtmp';
#use constant PHOTODIR  => '/home/guest/tmp/deleteme';
#use constant PHOTODIR  => '/usr/local/share/media/photo/';

###########################
## Function Declarations ##
###########################
sub postTag($$);                      # post a new tag of name and parent
sub postPhoto($$);                    # post a new photo given json and parent 
sub scanAudioDir($$);                 # reflect filesystem (tags) and audio
sub scanPhotoDir($$);                 # reflect filesystem (tags) and photos
sub readPhotoInfo($);                 # inspect the photo file
sub readOggInfo($);                   # inspect the ogg vorbis file
sub inspectPhotoDirFiles();           # function to pass to find()
sub inspectAudioDirFiles();           # function to pass to find()
sub hashToJson($);                    # function converts hash to json string
sub jsonToHash($);                    # function converts json string to hash
sub parseOggString($);                # hash suitable as json (from ogginfo)

###############
### GLOBALS ###
###############
my $debug   = TRUE;                   # are we debugging
my %found   = ();                     # results of $File::Find (must be global)


###############
### MAIN    ###
###############
#die("usage: " .$0 ."\n") if ($#ARGV != 1);
#scanPhotoDir(35, PHOTODIR);
scanAudioDir(35, AUDIODIR);


##########################
### PURPOSE: This routine scans the filesystem from the given directory for
###          photos to be added under the given tag.
### PARAMETERS: tag to attach data to, directory from which to scan
### RETURNS: hash of tags and photos to insert, posts new tags and photos
### GLOBALS: $debug
##########################
sub scanPhotoDir($$)
{
	my %retval = ();
	my $tagId = shift();
	my $top = shift();
	my $flag = FALSE;
	%found = ();
	print "recursing filesystem from ".$top."\n";
	find(\&inspectPhotoDirFiles, $top);
	#print Dumper(\%found) if ($debug);

	foreach my $dir (sort keys %found) {
		my $parent = dirname($dir);
		my $parentTagId = $tagId;
		print "processing dir \"".$dir."\"\n";

		if ((!exists $found{$parent}{"tagId"}
				|| !$found{$parent}{"tagId"}) && !$flag) {
			print "parent \"$parent\" not found, posting tag\n";
			my $tag = postTag(basename($parent), $parentTagId);
			#print Dumper($tag) if ($debug);
			$retval{"tags"}{$tag->{"id"}} = $tag;
			$found{$parent}{"tagId"} = $tag->{"id"};
			$flag = TRUE;
		}
		$parentTagId = $found{$parent}{"tagId"};

		if (!exists $found{$dir}{"tagId"} || !$found{$dir}{"tagId"}) {
			print "tag \"$dir\" not found, posting tag\n";
			my $tag = postTag(basename($dir), $parentTagId);
			#print Dumper($tag) if ($debug);
			$retval{"tags"}{$tag->{"id"}} = $tag;
			$found{$dir}{"tagId"} = $tag->{"id"};
		}
		$parentTagId = $found{$dir}{"tagId"};

		foreach my $photoDat (sort keys($found{$dir})) {
			my $json = hashToJson($found{$dir}{$photoDat});
			my $photo = postPhoto($json, $parentTagId);
			#print "json \"$json\"\n" if ($debug);
			$retval{"photos"}{$photo{"id"}} = $photo;
			#print Dumper($photo) if ($debug);
		}
	}
	print Dumper(\%found) if ($debug);
	%found = ();
	return(\%retval);
}

sub scanAudioDir($$)
{
	my %retval = ();
	my $tagId = shift();
	my $top = shift();
	%found = ();
	print "recursing filesystem from ".$top."\n";
	find(\&inspectAudioDirFiles, $top);

	foreach my $dir (sort keys(%found)) {
		print "processing dir ".$dir."\n";

		foreach my $audioDat (sort keys($found{$dir})) {
			my $audio = postAudio(
				hashToJson($found{$dir}{$audioDat}), $tagId);
			$retval{"audio"}{$audio{"id"}} = $audio;
		}
	}
	%found = ();
	return(\%retval);
}

sub inspectPhotoDirFiles()
{
	my $dirname = $File::Find::dir;

	if (-d $_) {
		$found{$dirname}{"tagId"} = undef;
	}
	else {
		$found{$dirname}{$_} = readPhotoInfo($File::Find::name);
	}
}

sub inspectAudioDirFiles()
{
	my $dirname = $File::Find::dir;
	$found{$dirname}{$_} = readOggInfo($File::Find::name) unless (-d $_);
}

sub readPhotoInfo($)
{
	my %retval = ();
	my $file = shift();
	my $path = "$file";
	my $name = basename($file);
	my $top = PHOTODIR;
	$path =~ s#$top##;
	$name =~ s/^(.{15}).*$/\1/;
	$file =~ s/\'/\\'/;
	$retval{"name"} = $name;
	$retval{"path"} = $path;
	$retval{"description"} = `rdjpgcom '$file' 2>/dev/null`;
	$retval{"description"} =~ s/\n//g;
	return(\%retval);
}

sub readOggInfo($)
{
	my %retval = ();
	my $file = shift();
	my $ogginfo = `ogginfo '$file'`;
	my $name = basename($file);
	my $parsed = parseOggString($ogginfo);
	$name =~ s/^(.{15}).*$/\1/;
	$retval{"name"} = $name;
	$retval{"path"} = $file;
	@retval{ keys $parsed } = values $parsed;
	return(\%retval);
}

sub parseOggString($)
{
	my %retval = ();
	my $ogginfo = shift();
	$retval{"title"} = $ogginfo;
	$retval{"title"} =~ s/^.*title=([^\n]+)\n.*$/\1/s;
	$retval{"artist"} = $ogginfo;
	$retval{"artist"} =~ s/^.*artist=([^\n]+)\n.*$/\1/s;
	$retval{"genre"} = $ogginfo;
	$retval{"genre"} =~ s/^.*genre=([^\n]+)\n.*$/\1/s;
	$retval{"genre"} =~ s/ /_/g;
	$retval{"genre"} = lc($retval{"genre"});
	$retval{"album"} = $ogginfo;
	$retval{"album"} =~ s/^.*album=([^\n]+)\n.*$/\1/s;
	$retval{"description"} = "purchased from apple.com";
	#$retval{"description"} = $ogginfo;
	#$retval{"description"} =~ s/^.*WARNING: Comment.*: "([^"]+)".*$/\1/s;
	return(\%retval);
}

sub postTag($$)
{
	my %retval = ();
	my $tagName = shift();
	my $parent = shift();
	my $host = WEBHOST;
	my $rslt = `curl -s -X POST -H "Accept:application/json" -H "Content-Type:application/json" -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" http://$host:8080/apis/v1/tags -d '{"name":"$tagName","parent":$parent}'`;
	print "$rslt\n";
	$retval = jsonToHash($rslt);
	return($retval);
}

sub postPhoto($$)
{
	my %retval = ();
	my $json = shift();
	my $tagId = shift();
	my $host = WEBHOST;
	my $rslt = `curl -s -X POST -H "Accept:application/json" -H "Content-Type:application/json" -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" http://$host:8080/apis/v1/photos?tagId=$tagId -d '$json'`;
	print "$rslt\n";
	%retval = %{jsonToHash($rslt)};
	return(\%retval);
}

sub postAudio($$)
{
	my %retval = ();
	my $json = shift();
	my $tagId = shift();
	my $host = WEBHOST;
	$json =~ s/'/_/g;
	my $rslt = `curl -s -X POST -H "Accept:application/json" -H "Content-Type:application/json" -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" http://$host:8080/apis/v1/sounds?tagId=$tagId -d '$json'`;
	print $rslt."\n";
	$retval = jsonToHash($rslt);
	return($retval);
}

sub hashToJson($)
{
	my $retval = "{";
	my $dat = shift();
	my $flag = FALSE;

	foreach my $key (sort keys(%{$dat})) {
		if ($flag) {
			$retval .= ",";
		}
		my $val = $dat->{$key};
		$val =~ s/['"]//g;
		$val =~ s#/.*$##g unless ($key =~ /path/);
		$val =~ s/[-&\\]/_/g unless ($key =~ /path/);
		$flag = TRUE;
		$retval .= "\"$key\":\"".$val."\"";
	}
	$retval .= "}";
	return($retval);
}

sub jsonToHash($)
{
	my %retval = ();
	my $json = shift();
	$json =~ s/"group":[^}]+\},//;
	$json =~ s/^[^{]*\{(.*)\}[^}]*$/\1/;

	foreach my $pair (split(",", $json)) {
		my ($key,$val) = split(":", $pair, 2);
		$key =~ s/\"//g;
		$val =~ s/\"//g;
		$retval{$key} = $val;
	}
	return(\%retval);
}
